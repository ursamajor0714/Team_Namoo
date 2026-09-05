package com.example.team_navigation_server.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 1000, message = "댓글은 1000자 이내여야 합니다.")
    private String content;

    public String getContent() { return content; }
}
