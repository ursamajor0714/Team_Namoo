package com.example.team_navigation_server.classification;

import com.example.team_navigation_server.news.NewsArticle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 정치성향 분류 테스트 파이프라인:
 * 1. (배치) 클로드로 의미 기반 중복 기사 그룹핑 -> 그룹당 1건만 남김
 * 2. (개별) 클로드로 본문을 분류 모델이 읽기 좋은 형태로 정리
 * 3. 로컬 FastAPI(classification-api)의 klue/bert-base 모델이 진보/중립/보수/판단불가 반환
 * 4. DB 저장
 * 5. (개별) 클로드로 프론트에 보여줄 한 줄 설명 생성
 * 확인용 테스트 코드 - 검증 끝나면 이 패키지 전체를 삭제할 예정.
 */
@Service
public class NewsClassificationService {

    private static final Logger log = LoggerFactory.getLogger(NewsClassificationService.class);

    private final ClaudeApiClient claudeClient;
    private final ClassificationModelClient modelClient;
    private final NewsClassificationRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewsClassificationService(ClaudeApiClient claudeClient,
                                      ClassificationModelClient modelClient,
                                      NewsClassificationRepository repository) {
        this.claudeClient = claudeClient;
        this.modelClient = modelClient;
        this.repository = repository;
    }

    public List<NewsClassification> classifyAndSave(List<NewsArticle> articles) throws IOException {
        List<NewsArticle> survivors = dedupeByClaude(articles);

        List<NewsClassification> saved = new ArrayList<>();
        for (NewsArticle article : survivors) {
            try {
                String cleaned = cleanForModel(article);
                PoliticalLeaning leaning = modelClient.classify(article.title(), cleaned);
                String blurb = generateFrontendBlurb(article.title(), leaning);

                NewsClassification entity = new NewsClassification(
                        article.title(), article.originalLink(), cleaned, leaning, blurb);
                saved.add(repository.save(entity));
            } catch (Exception e) {
                log.warn("기사 분류 실패, 건너뜀: {} ({})", article.originalLink(), e.getMessage());
            }
        }
        return saved;
    }

    private List<NewsArticle> dedupeByClaude(List<NewsArticle> articles) throws IOException {
        if (articles.size() <= 1) {
            return articles;
        }

        StringBuilder listing = new StringBuilder();
        for (int i = 0; i < articles.size(); i++) {
            listing.append(i).append(": ").append(articles.get(i).title())
                    .append(" - ").append(articles.get(i).description()).append("\n");
        }

        String system = "너는 뉴스 기사 제목/요약을 보고 같은 사건을 다루는 기사끼리 묶는 어시스턴트야. " +
                "결과는 반드시 JSON 배열만 출력해. 예: [[0,2],[1],[3,4,5]] " +
                "(같은 사건이면 같은 그룹, 다른 사건이면 별도 그룹). 다른 텍스트는 출력하지 마.";
        String response = claudeClient.complete(system, listing.toString(), 500);

        List<List<Integer>> groups = parseGroups(response, articles.size());
        List<NewsArticle> result = new ArrayList<>();
        for (List<Integer> group : groups) {
            if (!group.isEmpty()) {
                result.add(articles.get(group.get(0)));
            }
        }
        return result;
    }

    private List<List<Integer>> parseGroups(String response, int articleCount) {
        try {
            String jsonPart = response.substring(response.indexOf('['), response.lastIndexOf(']') + 1);
            JsonNode arr = objectMapper.readTree(jsonPart);
            List<List<Integer>> groups = new ArrayList<>();
            Set<Integer> seen = new HashSet<>();
            for (JsonNode group : arr) {
                List<Integer> indices = new ArrayList<>();
                for (JsonNode idx : group) {
                    int i = idx.asInt();
                    if (i >= 0 && i < articleCount && seen.add(i)) {
                        indices.add(i);
                    }
                }
                if (!indices.isEmpty()) {
                    groups.add(indices);
                }
            }
            return groups;
        } catch (Exception e) {
            log.warn("클로드 중복 그룹핑 응답 파싱 실패, 중복 제거 없이 진행: {}", e.getMessage());
            List<List<Integer>> fallback = new ArrayList<>();
            for (int i = 0; i < articleCount; i++) {
                fallback.add(List.of(i));
            }
            return fallback;
        }
    }

    private String cleanForModel(NewsArticle article) throws IOException {
        String system = "너는 뉴스 본문에서 광고, 기자 정보, 저작권 문구 등을 제거하고 " +
                "분류 모델이 읽기 좋은 핵심 본문만 500자 이내로 정리하는 어시스턴트야. 정리된 본문만 출력해.";
        String content = article.content() == null ? "" : article.content();
        String truncated = content.length() > 3000 ? content.substring(0, 3000) : content;
        return claudeClient.complete(system, "제목: " + article.title() + "\n\n본문:\n" + truncated, 400);
    }

    private String generateFrontendBlurb(String title, PoliticalLeaning leaning) throws IOException {
        String system = "너는 뉴스 카드에 보여줄 한 줄 설명을 작성하는 어시스턴트야. " +
                "제목과 분류 결과(" + leaning.getLabel() + ")를 참고해서 30자 이내 한국어 한 줄로 출력해. 다른 텍스트는 출력하지 마.";
        return claudeClient.complete(system, "제목: " + title, 100);
    }
}
