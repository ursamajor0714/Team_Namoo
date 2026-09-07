package com.example.team_navigation_server.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * oauth.google.* / oauth.naver.* 바인딩. 값은 application-local.properties(로컬) 또는
 * 배포 env(GOOGLE_CLIENT_ID 등)로 주입한다 - naver 뉴스검색 API 키(naver.client-id)와는
 * 별개의 네이버 개발자센터 애플리케이션(로그인용)이어야 한다.
 */
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private final Provider google = new Provider();
    private final Provider naver = new Provider();

    public Provider getGoogle() {
        return google;
    }

    public Provider getNaver() {
        return naver;
    }

    public static class Provider {
        private String clientId = "";
        private String clientSecret = "";
        private String redirectUri = "";

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }
}
