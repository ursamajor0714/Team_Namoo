package com.example.team_navigation_server.news;

/**
 * 네이버 뉴스 검색 API가 반환하는 개별 검색 결과(원본).
 * title/description은 HTML 태그와 엔티티가 제거된 순수 텍스트로 저장한다.
 */
public record NaverNewsItem(
        String title,
        String originalLink,
        String link,
        String description,
        String pubDate
) {
}
