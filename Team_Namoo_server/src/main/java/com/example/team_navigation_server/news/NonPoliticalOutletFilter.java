package com.example.team_navigation_server.news;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * 연예/스포츠 전문 매체 도메인 필터.
 * 네이버 뉴스 검색 API는 순수 텍스트 매칭이라 "정치"로 검색해도 "정치극"(드라마 장르), "정치 9단"(예능 자막)처럼
 * 실제 정치 기사가 아닌 연예/스포츠 기사가 섞여 들어온다. 이 앱은 정치 뉴스만 다루므로, 정치 기사를 사실상
 * 다루지 않는 연예/스포츠 전문지는 originalLink의 도메인 기준으로 걸러낸다.
 * 종합지(조선일보, 동아일보 등)는 정치 섹션이 있으므로 여기 포함하지 않는다 - 걸러내는 건 오직
 * "연예/스포츠만 다루는" 자매지/전문지(스포츠조선, 일간스포츠 등)뿐이다.
 */
public final class NonPoliticalOutletFilter {

    private static final Set<String> ENTERTAINMENT_SPORTS_DOMAINS = Set.of(
            "sports.chosun.com",     // 스포츠조선
            "sportsseoul.com",       // 스포츠서울
            "isplus.com",            // 일간스포츠
            "sports.donga.com",      // 스포츠동아
            "osen.mt.co.kr",         // OSEN
            "xportsnews.com",        // 엑스포츠뉴스
            "tenasia.hankyung.com",  // 텐아시아
            "mydaily.co.kr",         // 마이데일리
            "starnewskorea.com",     // 스타뉴스
            "newsen.com",            // 뉴스엔
            "sportskhan.co.kr",      // 스포츠경향
            "kstardaily.com",        // 스타데일리
            "spotvnews.co.kr",       // SPOTV NEWS
            "joynews24.com"          // 조이뉴스24
    );

    private NonPoliticalOutletFilter() {
    }

    /**
     * originalLink가 연예/스포츠 전문 매체 도메인이면 true.
     * URL 파싱에 실패하거나 링크가 비어있으면 걸러내지 않는다(false).
     */
    public static boolean isBlocked(String originalLink) {
        String host = extractHost(originalLink);
        if (host == null) {
            return false;
        }
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        for (String blocked : ENTERTAINMENT_SPORTS_DOMAINS) {
            if (host.equals(blocked) || host.endsWith("." + blocked)) {
                return true;
            }
        }
        return false;
    }

    private static String extractHost(String originalLink) {
        if (originalLink == null || originalLink.isBlank()) {
            return null;
        }
        try {
            return new URI(originalLink).getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
