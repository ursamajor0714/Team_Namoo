package com.example.team_navigation_server.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportCreateRequest {

    @NotBlank(message = "신고 사유를 입력해주세요.")
    @Size(max = 500, message = "신고 사유는 500자 이내로 입력해주세요.")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
