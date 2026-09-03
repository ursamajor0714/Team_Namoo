package com.example.team_navigation_server.classification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * classification.model.base-url 바인딩. 분류 모델을 서빙하는 FastAPI
 * (classification-api/main.py) 주소. 배포 시 CLASSIFICATION_MODEL_BASE_URL 환경변수로 주입.
 */
@Component
@ConfigurationProperties(prefix = "classification.model")
public class ClassificationModelProperties {

    private String baseUrl = "http://localhost:8000";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
