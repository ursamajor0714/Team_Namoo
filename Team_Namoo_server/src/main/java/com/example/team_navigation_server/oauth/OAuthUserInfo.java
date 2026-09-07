package com.example.team_navigation_server.oauth;

public class OAuthUserInfo {
    private final String providerId;
    private final String email;
    private final boolean emailVerified;

    public OAuthUserInfo(String providerId, String email, boolean emailVerified) {
        this.providerId = providerId;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
