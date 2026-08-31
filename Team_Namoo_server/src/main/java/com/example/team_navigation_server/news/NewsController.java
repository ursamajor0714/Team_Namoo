package com.example.team_navigation_server.news;

import com.example.team_navigation_server.classification.ClassificationModelClient;
import com.example.team_navigation_server.classification.PoliticalLeaning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 예) GET /api/news/collect?query=정치&display=10&sort=sim
 * query를 안 넘기면 기본값 '정치'로 검색한다.
 * display는 원하는 총 수집 건수다. 100건이 넘으면 내부적으로 네이버 API를 자동 페이징 호출해서 모은다
 * (네이버 API 제약상 start+display가 1000을 넘을 수 없어 최대 약 1000건까지 가능).
 * summarize=true를 명시적으로 넘겨야 기사별 GPT 요약을 수행한다. 대량 수집 시 시간/비용 절감을 위해 기본값은 false.
 * 네이버 뉴스 검색 -> 기사 본문 크롤링(병렬) -> (선택)요약 -> JSON/CSV/jsonl 파일 저장까지 한 번에 수행한다.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class NewsController {

    private static final Logger log = LoggerFactory.getLogger(NewsController.class);

    private final NewsCollectionService collectionService;
    private final NewsExportService exportService;
    private final ClassificationModelClient classificationModelClient;

    public NewsController(NewsCollectionService collectionService, NewsExportService exportService,
                           ClassificationModelClient classificationModelClient) {
        this.collectionService = collectionService;
        this.exportService = exportService;
        this.classificationModelClient = classificationModelClient;
    }

    /**
     * 프론트 카드 목록용. 파일로 내보내지 않고 수집한 기사 목록을 그대로 JSON으로 반환한다.
     * track=false로 호출해 CollectedLinkStore에 기록을 남기지 않는다 - 새로고침할 때마다
     * 상위 기사가 "이미 본 기사"로 소모되어 계속 다른 기사만 나오는 것을 막기 위함.
     */
    @GetMapping("/api/news")
    public List<NewsArticle> list(
            @RequestParam(defaultValue = "정치") String query,
            @RequestParam(defaultValue = "12") int display,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "sim") String sort
    ) throws IOException {
        return collectionService.collect(query, display, start, sort, false, false);
    }

    /**
     * 모달 하단 태그용. 로컬 분류 모델(classification-api)만 호출한다 - Claude API 키가 없어도 동작한다.
     * 분류 서버가 안 떠 있거나 실패하면 leaning=null을 돌려주고, 프론트는 이 경우 태그를 그냥 안 보여준다.
     */
    @PostMapping("/api/news/classify")
    public ClassifyResponse classify(@RequestBody ClassifyRequest request) {
        try {
            PoliticalLeaning leaning = classificationModelClient.classify(request.title(), request.content());
            return new ClassifyResponse(leaning.getLabel());
        } catch (Exception e) {
            log.warn("기사 분류 실패(분류 서버 미기동 등): {}", e.getMessage());
            return new ClassifyResponse(null);
        }
    }

    public record ClassifyRequest(String title, String content) {
    }

    public record ClassifyResponse(String leaning) {
    }

    /**
     * 정당 페이지용. count건 모일 때까지 검색 결과를 여러 페이지 넘겨가며 걸러낸다.
     * 1순위: 제목에 partyKeyword(정당명)가 그대로 들어있으면 분류 모델을 부르지 않고 바로 포함시킨다
     *        (헤드라인에 정당명이 박혀있는 기사가 성향 분류보다 더 확실한 신호).
     * 2순위: 키워드가 없으면 leaning(진보/중립/보수/판단불가) 분류 결과로 판단한다.
     * 분류 서버가 안 떠 있으면 키워드 매치분만 남고 나머지는 빈 목록에 가깝게 나온다.
     */
    @GetMapping("/api/news/by-leaning")
    public List<NewsArticle> byLeaning(
            @RequestParam String leaning,
            @RequestParam(required = false) String partyKeyword,
            @RequestParam(defaultValue = "정치") String query,
            @RequestParam(defaultValue = "12") int count
    ) throws IOException {
        List<NewsArticle> matched = new ArrayList<>();
        int start = 1;
        int batchSize = 15;
        int maxAttempts = 4;

        for (int attempt = 0; attempt < maxAttempts && matched.size() < count; attempt++) {
            List<NewsArticle> batch = collectionService.collect(query, batchSize, start, "sim", false, false);
            if (batch.isEmpty()) {
                break;
            }
            for (NewsArticle article : batch) {
                if (matched.size() >= count) {
                    break;
                }
                if (partyKeyword != null && !partyKeyword.isBlank() && article.title().contains(partyKeyword)) {
                    matched.add(article);
                    continue;
                }
                try {
                    PoliticalLeaning result = classificationModelClient.classify(article.title(), article.content());
                    if (result.getLabel().equals(leaning)) {
                        matched.add(article);
                    }
                } catch (Exception e) {
                    log.warn("기사 분류 실패, 건너뜀: {} ({})", article.originalLink(), e.getMessage());
                }
            }
            start += batchSize;
        }

        return matched;
    }

    @GetMapping("/api/news/collect")
    public NewsExportService.ExportResult collect(
            @RequestParam(defaultValue = "정치") String query,
            @RequestParam(defaultValue = "10") int display,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "sim") String sort,
            @RequestParam(defaultValue = "false") boolean summarize
    ) throws IOException {
        List<NewsArticle> articles = collectionService.collect(query, display, start, sort, summarize);
        return exportService.export(articles, query.replaceAll("\\s+", "_"));
    }
}
