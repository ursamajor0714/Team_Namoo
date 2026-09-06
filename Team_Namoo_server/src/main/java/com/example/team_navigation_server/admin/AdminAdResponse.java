package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.ad.Ad;
import com.example.team_navigation_server.ad.AdSide;

import java.time.LocalDateTime;

public class AdminAdResponse {

    private final Long id;
    private final String page;
    private final AdSide side;
    private final String imageUrl;
    private final String linkUrl;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final String status;

    public AdminAdResponse(Ad ad) {
        this.id = ad.getId();
        this.page = ad.getPage();
        this.side = ad.getSide();
        this.imageUrl = ad.getImageUrl();
        this.linkUrl = ad.getLinkUrl();
        this.startAt = ad.getStartAt();
        this.endAt = ad.getEndAt();
        this.createdBy = ad.getCreatedBy() == null ? null : ad.getCreatedBy().getNickname();
        this.createdAt = ad.getCreatedAt();
        this.status = computeStatus(ad);
    }

    // 시작/종료를 둘 다 안 정한 경우만 "기간 미설정" - 하나만 있으면 그 기준으로 예약/노출중/종료를 계산한다.
    private static String computeStatus(Ad ad) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = ad.getStartAt();
        LocalDateTime end = ad.getEndAt();
        if (start == null && end == null) return "기간 미설정";
        if (start != null && now.isBefore(start)) return "예약";
        if (end != null && now.isAfter(end)) return "종료";
        return "노출 중";
    }

    public Long getId() { return id; }
    public String getPage() { return page; }
    public AdSide getSide() { return side; }
    public String getImageUrl() { return imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
}
