package com.example.team_navigation_server.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public NewsCollectionService(NaverNewsApiClient apiClient, ArticleTextExtractor textExtractor,
                                  NewsSummaryService summaryService) {
        this.apiClient = apiClient;
        this.textExtractor = textExtractor;
        this.summaryService = summaryService;
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
        List<NaverNewsItem> items = fetchAllItems(query, totalCount, start, sort);
        return scrapeAll(items, summarize);
    }

    /**
     * display 파라미터가 100을 넘으면 네이버 API를 여러 번 나눠 호출해서 모은다.
     * 네이버 API는 display가 10 미만이면 에러(SE02)를 내므로, 남은 개수가 10 미만이어도
     * 항상 최소 10을 요청한다 - 그 결과 totalCount보다 몇 건 더 모일 수 있어 마지막에 잘라낸다.
     * start가 1000을 넘어가거나(네이버 하드 리밋), 남은 start 여유가 최소 요청량(10)보다 적거나,
     * 더 이상 결과가 없으면 중단한다.
     */
    private List<NaverNewsItem> fetchAllItems(String query, int totalCount, int start, String sort)
            throws IOException {
        List<NaverNewsItem> allItems = new ArrayList<>();
        int currentStart = start;

        while (allItems.size() < totalCount && currentStart <= NAVER_MAX_START) {
            int startCapacity = NAVER_MAX_START - currentStart + 1;
            if (startCapacity < NAVER_MIN_DISPLAY_PER_CALL) {
                // 최소 요청량(10)조차 채울 start 여유가 없으면 더 못 가져온다
                break;
            }

            int remaining = totalCount - allItems.size();
            int callDisplay = Math.max(NAVER_MIN_DISPLAY_PER_CALL, Math.min(NAVER_MAX_DISPLAY_PER_CALL, remaining));
            callDisplay = Math.min(callDisplay, startCapacity);

            List<NaverNewsItem> page = apiClient.search(query, callDisplay, currentStart, sort);
            if (page.isEmpty()) {
                break;
            }

            allItems.addAll(page);
            currentStart += page.size();

            if (page.size() < callDisplay) {
                // 네이버가 요청보다 적게 줬다는 건 결과가 더 없다는 뜻
                break;
            }
        }

        // 마지막 페이지에서 최소 요청량(10)을 채우느라 totalCount보다 더 모였을 수 있어 잘라낸다
        if (allItems.size() > totalCount) {
            return allItems.subList(0, totalCount);
        }
        return allItems;
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
                    summary
            );
        } catch (Exception e) {
            log.warn("본문 추출 실패, 건너뜀: {} ({})", item.originalLink(), e.getMessage());
            return null;
        }
    }
}
