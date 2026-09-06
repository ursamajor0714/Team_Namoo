package com.example.team_navigation_server.ad;

public class AdPublicResponse {
    private final Long id;
    private final String imageUrl;
    private final String linkUrl;

    public AdPublicResponse(Ad ad) {
        this.id = ad.getId();
        this.imageUrl = ad.getImageUrl();
        this.linkUrl = ad.getLinkUrl();
    }

    public Long getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getLinkUrl() { return linkUrl; }
}
