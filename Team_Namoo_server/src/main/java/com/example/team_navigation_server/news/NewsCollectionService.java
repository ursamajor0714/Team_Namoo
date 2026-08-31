package com.example.team_navigation_server.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 네이버 뉴스 검색 -> 원문 크롤링(Jsoup)까지 전체 파이프라인을 조립한다.
 * 대량 수집(최대 1000건)을 위해 검색 API 페이징과 본문 크롤링 병렬처리를 지원한다.
 */
@Service
public class NewsCollectionService {

    private static final Logger log = LoggerFactory.getLogger(NewsCollectionService.class);

    // 네이버 검색 API 하드 제약: 1회 호출 display는 10~100 사이여야 하고, start 최대 1000
    private static final int NAVER_MIN_DISPLAY_PER_CALL = 10;
    private static final int NAVER_MAX_DISPLAY_PER_CALL = 100;
    private static final int NAVER_MAX_START = 1000;

    // 본문 크롤링은 네트워크 I/O 대기가 대부분이라 스레드풀로 병렬화한다.
    private static final int SCRAPE_CONCURRENCY = 16;

    private final NaverNewsApiClient apiClient;
    private final ArticleTextExtractor textExtractor;
    private final NewsSummaryService summaryService;
    private final CollectedLinkStore linkStore;

    public NewsCollectionService(NaverNewsApiClient apiClient, ArticleTextExtractor textExtractor,
                                  NewsSummaryService summaryService, CollectedLinkStore linkStore) {
        this.apiClient = apiClient;
        this.textExtractor = textExtractor;
        this.summaryService = summaryService;
        this.linkStore = linkStore;
    }

    /**
     * @param query      검색어
     * @param totalCount 수집하고 싶은 총 기사 수 (네이버 API 제약상 최대 1000건까지만 가능)
     * @param start      검색 시작 위치 (1~1000)
     * @param sort       정렬 방식
     * @param summarize  true면 기사마다 GPT 요약을 수행한다. 대량 수집 시에는 시간/비용 때문에 false 권장.
     */
    public List<NewsArticle> collect(String query, int totalCount, int start, String sort, boolean summarize)
            throws IOException {
        return collect(query, totalCount, start, sort, summarize, true);
    }

    /**
     * @param track false면 CollectedLinkStore를 조회/기록하지 않는다 - 화면에 그냥 보여주기만 하는 호출(새로고침 등)이
     *              "이미 봤던 기사"로 영구 기록되어 다음 요청부터 계속 다른(더 하위 순위) 기사만 나오는 것을 막기 위함.
     *              학습 데이터 수집(export)처럼 실행 간 중복을 막아야 할 때만 true로 호출한다.
     */
    public List<NewsArticle> collect(String query, int totalCount, int start, String sort, boolean summarize,
                                      boolean track) throws IOException {
        List<NaverNewsItem> items = fetchAllItems(query, totalCount, start, sort, track);
        List<NewsArticle> articles = scrapeAll(items, summarize);
        return dedupeByContent(articles, track);
    }

    /**
     * 링크는 다르지만 여러 언론사가 그대로 재배포한(연합뉴스 등 통신사 기사) 동일 기사를 걸러낸다.
     * 본문의 공백을 모두 제거한 뒤 해시로 지문(fingerprint)을 만들어 비교한다 -
     * 같은 실행 안에서의 중복은 항상 걸러내고, track이 true일 때만 CollectedLinkStore에 기록해두어
     * 이후 실행에서도 같은 기사가 다시 나오지 않게 한다.
     * 본문 추출이 실패해 내용이 비어 있는 기사는 비교할 수 없으므로 중복 판단에서 제외(항상 포함)한다.
     */
    private List<NewsArticle> dedupeByContent(List<NewsArticle> articles, boolean track) {
        Set<String> seenThisRun = new HashSet<>();
        List<NewsArticle> result = new ArrayList<>();
        List<String> newFingerprints = new ArrayList<>();

        for (NewsArticle article : articles) {
            String fingerprint = contentFingerprint(article.content());
            if (fingerprint == null) {
                result.add(article);
                continue;
            }
            if ((track && linkStore.isCollected(fingerprint)) || !seenThisRun.add(fingerprint)) {
                // 이전 실행이나 이번 실행에서 이미 나온 동일 본문(재배포 기사)라 건너뛴다
                continue;
            }
            newFingerprints.add(fingerprint);
            result.add(article);
        }

        if (track) {
            linkStore.markCollected(newFingerprints);
        }
        return result;
    }

    private String contentFingerprint(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String normalized = content.replaceAll("\\s+", "");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * display 파라미터가 100을 넘으면 네이버 API를 여러 번 나눠 호출해서 모은다.
     * 네이버 API는 display가 10 미만이면 에러(SE02)를 내므로, 남은 개수가 10 미만이어도
     * 항상 최소 10을 요청한다 - 그 결과 totalCount보다 몇 건 더 모일 수 있어 마지막에 잘라낸다.
     * start가 1000을 넘어가거나(네이버 하드 리밋), 남은 start 여유가 최소 요청량(10)보다 적거나,
     * 더 이상 결과가 없으면 중단한다.
     * track이 true면 이전에 이미 수집했던 기사(CollectedLinkStore에 기록된 링크)는 건너뛴다 -
     * 같은 쿼리로 여러 번 수집해도(관련도순이라 매번 같은 상위 기사가 나옴) 중복 없이 새 기사만 모이게 된다.
     */
    private List<NaverNewsItem> fetchAllItems(String query, int totalCount, int start, String sort, boolean track)
            throws IOException {
        // originalLink 기준으로 중복 기사를 제거한다(같은 기사가 여러 페이지에 걸쳐 다시 나오는 경우 방지).
        // LinkedHashMap이라 처음 나온 순서(관련도/최신순)는 그대로 유지된다.
        Map<String, NaverNewsItem> uniqueItems = new LinkedHashMap<>();
        int currentStart = start;

        while (uniqueItems.size() < totalCount && currentStart <= NAVER_MAX_START) {
            int startCapacity = NAVER_MAX_START - currentStart + 1;
            if (startCapacity < NAVER_MIN_DISPLAY_PER_CALL) {
                // 최소 요청량(10)조차 채울 start 여유가 없으면 더 못 가져온다
                break;
            }

            int remaining = totalCount - uniqueItems.size();
            int callDisplay = Math.max(NAVER_MIN_DISPLAY_PER_CALL, Math.min(NAVER_MAX_DISPLAY_PER_CALL, remaining));
            callDisplay = Math.min(callDisplay, startCapacity);

            List<NaverNewsItem> page = apiClient.search(query, callDisplay, currentStart, sort);
            if (page.isEmpty()) {
                break;
            }

            for (NaverNewsItem item : page) {
                if (track && linkStore.isCollected(item.originalLink())) {
                    // 예전 실행에서 이미 뽑았던 기사라 건너뛴다
                    continue;
                }
                uniqueItems.putIfAbsent(item.originalLink(), item);
            }
            currentStart += page.size();

            if (page.size() < callDisplay) {
                // 네이버가 요청보다 적게 줬다는 건 결과가 더 없다는 뜻
                break;
            }
        }

        // 마지막 페이지에서 최소 요청량(10)을 채우느라 totalCount보다 더 모였을 수 있어 잘라낸다
        List<NaverNewsItem> result = new ArrayList<>(uniqueItems.values());
        if (result.size() > totalCount) {
            result = result.subList(0, totalCount);
        }

        if (track) {
            // 이번에 새로 뽑은 기사들을 기록해서, 다음 수집 때는 다시 나오지 않게 한다
            linkStore.markCollected(result.stream().map(NaverNewsItem::originalLink).toList());
        }

        return result;
    }

    /**
     * 개별 기사의 본문 크롤링이 실패해도(차단, 타임아웃 등) 전체 작업은 멈추지 않고
     * 해당 기사만 건너뛴 뒤 계속 진행한다. 스레드풀로 병렬 크롤링해 대량 수집 시간을 줄인다.
     */
    private List<NewsArticle> scrapeAll(List<NaverNewsItem> items, boolean summarize) {
        if (items.isEmpty()) {
            return List.of();
        }

        int poolSize = Math.min(SCRAPE_CONCURRENCY, items.size());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<NewsArticle> articles = new ArrayList<>();

        try {
            List<Future<NewsArticle>> futures = new ArrayList<>();
            for (NaverNewsItem item : items) {
                Callable<NewsArticle> task = () -> scrapeOne(item, summarize);
                futures.add(executor.submit(task));
            }

            for (Future<NewsArticle> future : futures) {
                try {
                    NewsArticle article = future.get();
                    if (article != null) {
                        articles.add(article);
                    }
                } catch (Exception e) {
                    log.warn("기사 크롤링 작업 실패, 건너뜀: {}", e.getMessage());
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return articles;
    }

    private NewsArticle scrapeOne(NaverNewsItem item, boolean summarize) {
        try {
            ArticleTextExtractor.ExtractedArticle extracted = textExtractor.extract(item.originalLink());
            // 페이지에서 뽑은 제목이 비어 있으면 검색 API가 준 제목으로 대체
            String title = !extracted.title().isBlank() ? extracted.title() : item.title();
            // 요약 실패는 전체 수집을 막지 않는다(NewsSummaryService 내부에서 예외를 흡수함)
            String summary = summarize ? summaryService.summarize(title, extracted.content()) : "";
            return new NewsArticle(
                    title,
                    item.originalLink(),
                    item.link(),
                    item.description(),
                    item.pubDate(),
                    extracted.content(),
                    summary,
                    extracted.contentHtml(),
                    extracted.imageUrl()
            );
        } catch (Exception e) {
            log.warn("본문 추출 실패, 건너뜀: {} ({})", item.originalLink(), e.getMessage());
            return null;
        }
    }
}
