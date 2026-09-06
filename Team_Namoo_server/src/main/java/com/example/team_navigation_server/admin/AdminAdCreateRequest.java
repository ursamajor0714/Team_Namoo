package com.example.team_navigation_server.admin;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class AdminAdCreateRequest {

    @NotBlank(message = "게재 위치(page)를 입력해주세요.")
    private String page;

    @NotBlank(message = "슬롯 위치(side)를 입력해주세요.")
    private String side;

    private String imageUrl;
    private String linkUrl;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
}
