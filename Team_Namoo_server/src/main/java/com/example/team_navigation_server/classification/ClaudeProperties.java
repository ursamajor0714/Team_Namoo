package com.example.team_navigation_server.classification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.properties의 claude.api-key / claude.model 값을 바인딩한다.
 * 정치성향 분류 테스트 파이프라인 전용 - 확인 끝나면 이 패키지 전체를 삭제할 예정.
 */
@Component
@ConfigurationProperties(prefix = "claude")
public class ClaudeProperties {

    private String apiKey;
    private String model = "claude-sonnet-4-5";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("YOUR_CLAUDE_API_KEY");
    }
}
