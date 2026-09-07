package com.example.team_navigation_server.board;

import jakarta.validation.constraints.NotBlank;

public class PostVoteRequest {

    @NotBlank(message = "추천 유형을 입력해주세요.")
    private String type;

    public String getType() {
        return type;
    }
}
