package com.example.team_navigation_server.news;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Jsoup으로 추출한 기사 본문을 OpenAI Chat Completion API로 3~4문장 한국어 요약한다.
 * openai.api-key가 설정되어 있지 않으면(플레이스홀더 그대로거나 공백) 요약 없이 빈 문자열을 반환하고
 * 전체 수집 파이프라인은 그대로 계속 진행한다.
 */
@Service
public class NewsSummaryService {

    private static final Logger log = LoggerFactory.getLogger(NewsSummaryService.class);

    // 토큰/비용 절약을 위해 본문 앞부분만 요약에 사용한다.
    private static final int MAX_CONTENT_CHARS = 4000;
    private static final String SYSTEM_PROMPT =
            "너는 뉴스 기사를 3~4문장으로 간결하게 요약하는 어시스턴트야. 사실관계만 담백하게 요약해.";

    private final OpenAiProperties properties;
    private volatile OpenAiService openAiService;

    public NewsSummaryService(OpenAiProperties properties) {
        this.properties = properties;
    }

    public String summarize(String title, String content) {
        if (!properties.isConfigured() || content == null || content.isBlank()) {
            return "";
        }

        try {
            OpenAiService service = getOrCreateService();
            String truncated = content.length() > MAX_CONTENT_CHARS
                    ? content.substring(0, MAX_CONTENT_CHARS)
                    : content;

            List<ChatMessage> messages = List.of(
                    new ChatMessage("system", SYSTEM_PROMPT),
                    new ChatMessage("user", "제목: " + title + "\n\n본문:\n" + truncated)
            );

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(properties.getModel())
                    .messages(messages)
                    .maxTokens(300)
                    .temperature(0.3)
                    .build();

            return service.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent().trim();
        } catch (Exception e) {
            log.warn("기사 요약 실패, 빈 요약으로 진행: {}", e.getMessage());
            return "";
        }
    }

    private OpenAiService getOrCreateService() {
        if (openAiService == null) {
            synchronized (this) {
                if (openAiService == null) {
                    openAiService = new OpenAiService(properties.getApiKey(), Duration.ofSeconds(30));
                }
            }
        }
        return openAiService;
    }
}
