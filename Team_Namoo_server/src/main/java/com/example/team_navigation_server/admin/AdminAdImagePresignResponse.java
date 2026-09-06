package com.example.team_navigation_server.admin;

public class AdminAdImagePresignResponse {

    private final String uploadUrl;
    private final String imageUrl;

    public AdminAdImagePresignResponse(String uploadUrl, String imageUrl) {
        this.uploadUrl = uploadUrl;
        this.imageUrl = imageUrl;
    }

    public String getUploadUrl() { return uploadUrl; }
    public String getImageUrl() { return imageUrl; }
}
