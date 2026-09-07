package com.example.team_navigation_server.oauth;

import com.example.team_navigation_server.member.OAuthProvider;

import java.io.IOException;

public interface OAuthClient {

    OAuthProvider provider();

    String authorizeUrl(String state);

    OAuthUserInfo exchange(String code) throws IOException;
}
