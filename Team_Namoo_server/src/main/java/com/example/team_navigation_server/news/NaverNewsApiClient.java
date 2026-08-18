package com.example.team_navigation_server.news;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * NAVER API HUB의 뉴스 검색 API를 호출하는 클라이언트.
 * 2026년 6월 기존 Developers Center 검색 API가 NAVER API HUB로 이관되면서
 * 도메인/경로/인증 헤더가 아래와 같이 바뀌었다.
 *   - 기존: https://openapi.naver.com/v1/search/news.json (X-Naver-Client-Id / X-Naver-Client-Secret)
 *   - 신규: https://naverapihub.apigw.ntruss.com/search/v1/news (X-NCP-APIGW-API-KEY-ID / X-NCP-APIGW-API-KEY)
 * 응답의 items(title/originallink/link/description/pubDate) 구조는 동일하게 유지된다.
 */
@Component
public class NaverNewsApiClient {

    private static final String SEARCH_URL = "https://naverapihub.apigw.ntruss.com/search/v1/news";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NaverNewsProperties properties;

    public NaverNewsApiClient(NaverNewsProperties properties) {
        this.properties = properties;
    }

    /**
     * @param query   검색어
     * @param display 검색 결과 출력 건수 (1~100)
     * @param start   검색 시작 위치 (1~1000)
     * @param sort    정렬 방식: sim(정확도순, 기본) 또는 date(최신순)
     */
    public List<NaverNewsItem> search(String query, int display, int start, String sort) throws IOException {
        if (properties.getClientId() == null || properties.getClientId().isBlank()
                || properties.getClientId().equals("YOUR_NAVER_CLIENT_ID")) {
            throw new IllegalStateException(
                    "naver.client-id / naver.client-secret 이 설정되지 않았습니다. application.properties를 확인하세요.");
        }

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SEARCH_URL)).newBuilder()
                .addQueryParameter("query", query)
                .addQueryParameter("display", String.valueOf(display))
                .addQueryParameter("start", String.valueOf(start))
                .addQueryParameter("sort", sort)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-NCP-APIGW-API-KEY-ID", properties.getClientId())
                .addHeader("X-NCP-APIGW-API-KEY", properties.getClientSecret())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("네이버 뉴스 API 호출 실패: HTTP " + response.code() + " " + body);
            }

            JsonNode root = objectMapper.readTree(body);
            JsonNode items = root.path("items");

            List<NaverNewsItem> result = new ArrayList<>();
            for (JsonNode item : items) {
                result.add(new NaverNewsItem(
                        cleanHtml(item.path("title").asText("")),
                        item.path("originallink").asText(""),
                        item.path("link").asText(""),
                        cleanHtml(item.path("description").asText("")),
                        item.path("pubDate").asText("")
                ));
            }
            return result;
        }
    }

    /**
     * 네이버 API는 검색어 하이라이트를 위해 title/description에 &lt;b&gt; 태그와
     * HTML 엔티티(&amp;quot; 등)를 포함시켜 반환한다. Jsoup으로 태그 제거 + 엔티티 해석을 한번에 처리한다.
     */
    private String cleanHtml(String raw) {
        return Jsoup.parse(raw).text();
    }
}
