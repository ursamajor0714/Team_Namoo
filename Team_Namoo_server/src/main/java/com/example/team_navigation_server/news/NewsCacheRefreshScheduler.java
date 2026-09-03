package com.example.team_navigation_server.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 정당 페이지/뉴스 목록이 매 요청마다 라이브 크롤링+분류를 반복해서 응답이 느려지는 문제를 막기 위해,
 * 30분마다 백그라운드에서 최신 정치 기사를 미리 수집해서 캐시에 채워둔다.
 * fixedRate라 앱 기동 직후 한 번 즉시 실행되고, 그 뒤로는 30분 간격으로 반복된다.
 */
@Component
public class NewsCacheRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(NewsCacheRefreshScheduler.class);
    private static final long REFRESH_INTERVAL_MS = 30 * 60 * 1000;

    private final NewsCacheService cacheService;

    public NewsCacheRefreshScheduler(NewsCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Scheduled(fixedRate = REFRESH_INTERVAL_MS)
    public void refresh() {
        log.info("뉴스 캐시 갱신 스케줄 실행");
        cacheService.refresh();
    }
}
