package com.example.team_navigation_server.oauth;

import com.example.team_navigation_server.member.MemberResponse;

public class OAuthCallbackResponse {

    private final String status; // LOGIN | SIGNUP_REQUIRED
    private final MemberResponse member;
    private final String email;
    private final String suggestedNickname;

    private OAuthCallbackResponse(String status, MemberResponse member, String email, String suggestedNickname) {
        this.status = status;
        this.member = member;
        this.email = email;
        this.suggestedNickname = suggestedNickname;
    }

    public static OAuthCallbackResponse login(MemberResponse member) {
        return new OAuthCallbackResponse("LOGIN", member, null, null);
    }

    public static OAuthCallbackResponse signupRequired(String email, String suggestedNickname) {
        return new OAuthCallbackResponse("SIGNUP_REQUIRED", null, email, suggestedNickname);
    }

    public String getStatus() {
        return status;
    }

    public MemberResponse getMember() {
        return member;
    }

    public String getEmail() {
        return email;
    }

    public String getSuggestedNickname() {
        return suggestedNickname;
    }
}
