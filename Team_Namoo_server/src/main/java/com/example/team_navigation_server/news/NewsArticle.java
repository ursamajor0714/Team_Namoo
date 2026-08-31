package com.example.team_navigation_server.news;

/**
 * 검색 API 메타데이터 + Jsoup으로 추출한 본문(content) + OpenAI 요약(summary)을 합친 최종 결과.
 * 이 형태 그대로 JSON/CSV로 저장되어 콜랩 학습 데이터로 쓰인다.
 */
public record NewsArticle(
        String title,
        String originalLink,
        String link,
        String description,
        String pubDate,
        String content,
        String summary,
        String contentHtml,
        String imageUrl
) {
}
