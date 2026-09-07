package com.example.team_navigation_server.admin;

import jakarta.validation.constraints.NotBlank;

public class AdminReportStatusRequest {

    @NotBlank(message = "처리 상태를 입력해주세요.")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
