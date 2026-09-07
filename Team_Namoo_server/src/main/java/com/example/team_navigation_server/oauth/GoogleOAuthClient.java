package com.example.team_navigation_server.oauth;

import com.example.team_navigation_server.member.OAuthProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 구글 로그인 (OAuth2 authorization code flow). 콘솔: https://console.cloud.google.com/apis/credentials
 * 승인된 리디렉션 URI에 oauth.google.redirect-uri 값과 정확히 일치하는 주소를 등록해야 한다.
 */
@Component
public class GoogleOAuthClient implements OAuthClient {

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OAuthProperties properties;

    public GoogleOAuthClient(OAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public String authorizeUrl(String state) {
        OAuthProperties.Provider p = properties.getGoogle();
        return AUTHORIZE_URL
                + "?client_id=" + encode(p.getClientId())
                + "&redirect_uri=" + encode(p.getRedirectUri())
                + "&response_type=code"
                + "&scope=" + encode("email profile")
                + "&state=" + encode(state);
    }

    @Override
    public OAuthUserInfo exchange(String code) throws IOException {
        OAuthProperties.Provider p = properties.getGoogle();

        Request tokenRequest = new Request.Builder()
                .url(TOKEN_URL)
                .post(new FormBody.Builder()
                        .add("code", code)
                        .add("client_id", p.getClientId())
                        .add("client_secret", p.getClientSecret())
                        .add("redirect_uri", p.getRedirectUri())
                        .add("grant_type", "authorization_code")
                        .build())
                .build();

        String accessToken;
        try (Response response = httpClient.newCall(tokenRequest).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("구글 토큰 발급 실패: HTTP " + response.code() + " " + body);
            }
            accessToken = objectMapper.readTree(body).path("access_token").asText(null);
            if (accessToken == null) {
                throw new IOException("구글 토큰 응답에 access_token이 없습니다: " + body);
            }
        }

        Request userInfoRequest = new Request.Builder()
                .url(USERINFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = httpClient.newCall(userInfoRequest).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("구글 사용자 정보 조회 실패: HTTP " + response.code() + " " + body);
            }
            JsonNode node = objectMapper.readTree(body);
            String sub = node.path("sub").asText(null);
            String email = node.path("email").asText(null);
            boolean emailVerified = node.path("email_verified").asBoolean(false);
            return new OAuthUserInfo(sub, email, emailVerified);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
