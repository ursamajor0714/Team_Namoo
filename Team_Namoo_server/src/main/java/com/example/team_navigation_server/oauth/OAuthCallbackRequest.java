package com.example.team_navigation_server.oauth;

import jakarta.validation.constraints.NotBlank;

public class OAuthCallbackRequest {

    @NotBlank(message = "code가 필요합니다.")
    private String code;

    @NotBlank(message = "state가 필요합니다.")
    private String state;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
