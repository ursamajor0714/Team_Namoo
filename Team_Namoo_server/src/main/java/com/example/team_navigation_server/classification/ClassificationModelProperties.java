package com.example.team_navigation_server.classification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.properties의 classification.model.base-url 값을 바인딩한다.
 * 콜랩에서 학습한 klue/bert-base 모델을 로컬 FastAPI(classification-api/main.py)로 서빙한 주소.
 * 정치성향 분류 테스트 파이프라인 전용 - 확인 끝나면 이 패키지 전체를 삭제할 예정.
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
