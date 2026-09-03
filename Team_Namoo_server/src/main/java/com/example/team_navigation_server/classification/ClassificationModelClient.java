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
 * klue/bert-base 파인튜닝 모델을 서빙하는 FastAPI(classification-api/main.py) 호출 클라이언트.
 * NewsController 의 POST /api/news/classify 와 뉴스 캐시 분류에서 실제로 사용한다.
 * base-url 은 classification.model.base-url (배포 시 CLASSIFICATION_MODEL_BASE_URL 환경변수).
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
