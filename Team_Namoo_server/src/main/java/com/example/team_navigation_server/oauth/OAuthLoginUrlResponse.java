package com.example.team_navigation_server.oauth;

public class OAuthLoginUrlResponse {

    private final String url;

    public OAuthLoginUrlResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
