package com.example.team_navigation_server.admin;

import jakarta.validation.constraints.NotBlank;

public class AdminPostVisibilityRequest {

    @NotBlank(message = "노출 상태를 입력해주세요.")
    private String visibility;

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
}
