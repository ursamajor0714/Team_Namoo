package com.example.team_navigation_server.oauth;

import com.example.team_navigation_server.member.OAuthProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * 네이버 로그인 (OAuth2 authorization code flow). 콘솔: https://developers.naver.com/apps
 * 뉴스검색 API(naver.client-id)와는 별개의 애플리케이션을 "네이버 로그인" 기능으로 등록해야 한다 -
 * 서비스 URL/콜백 URL을 oauth.naver.redirect-uri 값과 정확히 일치시킬 것.
 * 네이버는 이메일 인증 여부를 응답에 안 주지만, 네이버 자체가 가입 시 이메일/휴대폰 인증을 요구하므로
 * emailVerified=true 로 취급한다(구글처럼 명시적 플래그가 없는 것과의 차이).
 */
@Component
public class NaverOAuthClient implements OAuthClient {

    private static final String AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";
    private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String USERINFO_URL = "https://openapi.naver.com/v1/nid/me";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OAuthProperties properties;

    public NaverOAuthClient(OAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.NAVER;
    }

    @Override
    public String authorizeUrl(String state) {
        OAuthProperties.Provider p = properties.getNaver();
        return HttpUrl.parse(AUTHORIZE_URL).newBuilder()
                .addQueryParameter("response_type", "code")
                .addQueryParameter("client_id", p.getClientId())
                .addQueryParameter("redirect_uri", p.getRedirectUri())
                .addQueryParameter("state", state)
                .build()
                .toString();
    }

    @Override
    public OAuthUserInfo exchange(String code) throws IOException {
        OAuthProperties.Provider p = properties.getNaver();

        Request tokenRequest = new Request.Builder()
                .url(HttpUrl.parse(TOKEN_URL).newBuilder()
                        .addQueryParameter("grant_type", "authorization_code")
                        .addQueryParameter("client_id", p.getClientId())
                        .addQueryParameter("client_secret", p.getClientSecret())
                        .addQueryParameter("code", code)
                        .build())
                .get()
                .build();

        String accessToken;
        try (Response response = httpClient.newCall(tokenRequest).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("네이버 토큰 발급 실패: HTTP " + response.code() + " " + body);
            }
            accessToken = objectMapper.readTree(body).path("access_token").asText(null);
            if (accessToken == null) {
                throw new IOException("네이버 토큰 응답에 access_token이 없습니다: " + body);
            }
        }

        Request userInfoRequest = new Request.Builder()
                .url(USERINFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = httpClient.newCall(userInfoRequest).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("네이버 사용자 정보 조회 실패: HTTP " + response.code() + " " + body);
            }
            JsonNode result = objectMapper.readTree(body).path("response");
            String id = result.path("id").asText(null);
            String email = result.path("email").asText(null);
            return new OAuthUserInfo(id, email, true);
        }
    }
}
