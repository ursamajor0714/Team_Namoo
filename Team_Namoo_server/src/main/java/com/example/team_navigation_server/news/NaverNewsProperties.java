package com.example.team_navigation_server.news;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.properties의 naver.client-id / naver.client-secret 값을 바인딩한다.
 * 값은 https://developers.naver.com/apps 에서 애플리케이션 등록 후 발급받을 수 있다.
 */
@Component
@ConfigurationProperties(prefix = "naver")
public class NaverNewsProperties {

    private String clientId;
    private String clientSecret;

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
}
