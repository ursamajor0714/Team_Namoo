package com.example.team_navigation_server.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AdminArticleBulkVisibilityRequest {

    @NotEmpty(message = "대상 기사를 선택해주세요.")
    private List<Long> ids;

    @NotBlank(message = "노출 상태를 입력해주세요.")
    private String visibility;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
}
