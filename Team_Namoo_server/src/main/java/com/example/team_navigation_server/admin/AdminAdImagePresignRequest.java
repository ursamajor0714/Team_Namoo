package com.example.team_navigation_server.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class AdminAdImagePresignRequest {

    @NotBlank(message = "이미지 형식(contentType)을 입력해주세요.")
    private String contentType;

    @Positive(message = "파일 크기가 올바르지 않습니다.")
    private long size;

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
