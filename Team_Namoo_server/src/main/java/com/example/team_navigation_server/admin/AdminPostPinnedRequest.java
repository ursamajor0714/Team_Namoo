package com.example.team_navigation_server.admin;

import jakarta.validation.constraints.NotNull;

public class AdminPostPinnedRequest {

    @NotNull(message = "공지 여부를 입력해주세요.")
    private Boolean pinned;

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }
}
