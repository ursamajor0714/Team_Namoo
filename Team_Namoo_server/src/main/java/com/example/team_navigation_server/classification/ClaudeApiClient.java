package com.example.team_navigation_server.classification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * Anthropic Messages API(https://api.anthropic.com/v1/messages) 호출을 담당하는 저수준 클라이언트.
 * 정치성향 분류 테스트 파이프라인 전용 - 확인 끝나면 이 패키지 전체를 삭제할 예정.
 */
@Component
public class ClaudeApiClient {

    private static final String MESSAGES_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(60))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClaudeProperties properties;

    public ClaudeApiClient(ClaudeProperties properties) {
        this.properties = properties;
    }

    /**
     * @return 클로드가 응답한 텍스트(첫 번째 content 블록)
     */
    public String complete(String systemPrompt, String userPrompt, int maxTokens) throws IOException {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("claude.api-key가 설정되지 않았습니다. application.properties를 확인하세요.");
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", properties.getModel());
        root.put("max_tokens", maxTokens);
        root.put("system", systemPrompt);
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        root.putArray("messages").add(userMessage);

        RequestBody body = RequestBody.create(JSON, objectMapper.writeValueAsString(root));
        Request request = new Request.Builder()
                .url(MESSAGES_URL)
                .addHeader("x-api-key", properties.getApiKey())
                .addHeader("anthropic-version", ANTHROPIC_VERSION)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("Claude API 호출 실패: HTTP " + response.code() + " " + responseBody);
            }
            JsonNode parsed = objectMapper.readTree(responseBody);
            return parsed.path("content").path(0).path("text").asText("");
        }
    }
}
