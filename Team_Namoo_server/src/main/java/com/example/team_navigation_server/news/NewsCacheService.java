package com.example.team_navigation_server.news;

import com.example.team_navigation_server.classification.ClassificationModelClient;
import com.example.team_navigation_server.classification.PoliticalLeaning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 뉴스 캐시 조회/갱신을 담당한다.
 * 정당 페이지/뉴스 목록이 매 요청마다 네이버 API 호출 + 크롤링 + 분류를 반복하면 응답이
 * 수십 초씩 걸리고 끊기던 문제가 있어서, 백그라운드(NewsCacheRefreshScheduler)에서 주기적으로
 * 미리 수집해둔 최근 WINDOW_DAYS일치 기사를 여기서 조회만 하도록 분리했다.
 */
@Service
public class NewsCacheService {

    private static final Logger log = LoggerFactory.getLogger(NewsCacheService.class);

    private static final int WINDOW_DAYS = 3;
    private static final int REFRESH_BATCH_SIZE = 30;
    private static final String REFRESH_QUERY = "정치";

    private final NewsCollectionService collectionService;
    private final ClassificationModelClient classificationModelClient;
    private final CachedNewsArticleRepository repository;

    public NewsCacheService(NewsCollectionService collectionService,
                             ClassificationModelClient classificationModelClient,
                             CachedNewsArticleRepository repository) {
        this.collectionService = collectionService;
        this.classificationModelClient = classificationModelClient;
        this.repository = repository;
    }

    /**
     * 최신 정치 기사를 수집해서 아직 캐시에 없는 것만 분류 후 저장하고, 3일보다 오래된 캐시는 지운다.
     * 개별 기사의 크롤링/분류 실패가 전체 갱신을 막지 않도록 기사 단위로 예외를 흡수한다.
     */
    public void refresh() {
        try {
            List<NewsArticle> articles = collectionService.collect(
                    REFRESH_QUERY, REFRESH_BATCH_SIZE, 1, "date", false, false);
            int savedCount = 0;
            for (NewsArticle article : articles) {
                if (repository.existsByOriginalLink(article.originalLink())) {
                    // 이전 주기에 이미 저장한 기사라 건너뛴다
                    continue;
                }
                saveOne(article);
                savedCount++;
            }
            log.info("뉴스 캐시 갱신 완료: 신규 {}건 저장", savedCount);
        } catch (Exception e) {
            log.warn("뉴스 캐시 갱신 실패, 다음 주기에 재시도: {}", e.getMessage());
        }
        pruneOld();
    }

    private void saveOne(NewsArticle article) {
        PoliticalLeaning leaning = classifySafely(article);
        CachedNewsArticle entity = new CachedNewsArticle(
                article.originalLink(), article.title(), article.link(), article.description(),
                article.pubDate(), article.content(), article.contentHtml(), article.summary(),
                article.imageUrl(), leaning, Instant.now());
        repository.save(entity);
    }

    private PoliticalLeaning classifySafely(NewsArticle article) {
        try {
            return classificationModelClient.classify(article.title(), article.content());
        } catch (Exception e) {
            log.warn("캐시 저장 중 기사 분류 실패(성향 없이 저장): {} ({})", article.originalLink(), e.getMessage());
            return null;
        }
    }

    private void pruneOld() {
        Instant cutoff = Instant.now().minus(WINDOW_DAYS, ChronoUnit.DAYS);
        long deleted = repository.deleteByCollectedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("{}일 지난 캐시 기사 {}건 삭제", WINDOW_DAYS, deleted);
        }
    }

    /**
     * 뉴스 목록(정당 무관)용. 캐시에서 최근순으로 count건 반환한다.
     */
    public List<NewsArticle> list(int count) {
        return recentPool().stream()
                .limit(count)
                .map(NewsCacheService::toNewsArticle)
                .toList();
    }

    /**
     * 정당 페이지용.
     * 1순위: 제목에 partyKeyword(정당명)가 그대로 들어있으면 분류 결과와 무관하게 채택한다
     *        (헤드라인에 정당명이 박혀있는 기사가 성향 분류보다 더 확실한 신호).
     * 2순위: 그것만으로 count가 안 채워지면 leaning(진보/중립/보수/판단불가)이 일치하는 기사로 채운다.
     */
    public List<NewsArticle> byLeaning(String leaningLabel, String partyKeyword, int count) {
        PoliticalLeaning leaning = PoliticalLeaning.fromLabel(leaningLabel);
        List<CachedNewsArticle> pool = recentPool();

        List<CachedNewsArticle> matched = new ArrayList<>();
        Set<String> added = new HashSet<>();

        if (partyKeyword != null && !partyKeyword.isBlank()) {
            for (CachedNewsArticle article : pool) {
                if (matched.size() >= count) {
                    break;
                }
                if (article.getTitle().contains(partyKeyword) && added.add(article.getOriginalLink())) {
                    matched.add(article);
                }
            }
        }

        for (CachedNewsArticle article : pool) {
            if (matched.size() >= count) {
                break;
            }
            if (article.getLeaning() == leaning && added.add(article.getOriginalLink())) {
                matched.add(article);
            }
        }

        return matched.stream().map(NewsCacheService::toNewsArticle).toList();
    }

    /** 회원용 - 노출 상태가 NORMAL인 기사만 (관리자가 감추거나 삭제 표시한 건 제외). */
    private List<CachedNewsArticle> recentPool() {
        Instant cutoff = Instant.now().minus(WINDOW_DAYS, ChronoUnit.DAYS);
        return repository.findByCollectedAtAfterAndVisibilityOrderByCollectedAtDesc(cutoff, ArticleVisibility.NORMAL);
    }

    private static NewsArticle toNewsArticle(CachedNewsArticle a) {
        return new NewsArticle(a.getTitle(), a.getOriginalLink(), a.getLink(), a.getDescription(), a.getPubDate(),
                a.getContent(), a.getSummary(), a.getContentHtml(), a.getImageUrl());
    }
}
