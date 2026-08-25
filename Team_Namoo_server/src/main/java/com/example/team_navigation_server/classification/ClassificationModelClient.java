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
 * 콜랩에서 학습한 klue/bert-base 모델을 서빙하는 로컬 FastAPI(classification-api/main.py) 호출 클라이언트.
 * MockClassificationModelClient를 대체한다.
 * 정치성향 분류 테스트 파이프라인 전용 - 확인 끝나면 이 패키지 전체를 삭제할 예정.
 */
@Component
public class ClassificationModelClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(90))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClassificationModelProperties properties;

    public ClassificationModelClient(ClassificationModelProperties properties) {
        this.properties = properties;
    }

    public PoliticalLeaning classify(String title, String body) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("title", title == null ? "" : title);
        root.put("body", body == null ? "" : body);

        RequestBody requestBody = RequestBody.create(JSON, objectMapper.writeValueAsString(root));
        Request request = new Request.Builder()
                .url(properties.getBaseUrl() + "/predict")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("분류 모델 호출 실패: HTTP " + response.code() + " " + responseBody);
            }
            JsonNode parsed = objectMapper.readTree(responseBody);
            String label = parsed.path("정치성향").asText("");
            return PoliticalLeaning.fromLabel(label);
        }
    }
}
