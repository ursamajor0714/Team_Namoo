package com.example.team_navigation_server.news;

import com.example.team_navigation_server.classification.ClassificationModelClient;
import com.example.team_navigation_server.classification.PoliticalLeaning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
public class NewsController {

    private static final Logger log = LoggerFactory.getLogger(NewsController.class);

    private final NewsCollectionService collectionService;
    private final NewsExportService exportService;
    private final ClassificationModelClient classificationModelClient;
    private final NewsCacheService cacheService;

    public NewsController(NewsCollectionService collectionService, NewsExportService exportService,
                           ClassificationModelClient classificationModelClient, NewsCacheService cacheService) {
        this.collectionService = collectionService;
        this.exportService = exportService;
        this.classificationModelClient = classificationModelClient;
        this.cacheService = cacheService;
    }

    /**
     * 프론트 카드 목록용. 매 요청마다 라이브 크롤링하지 않고, NewsCacheRefreshScheduler가 미리
     * 채워둔 최근 3일치 캐시(NewsCacheService)에서 최신순으로 꺼내 온다 - 응답이 즉시 온다.
     */
    @GetMapping("/api/news")
    public List<NewsArticle> list(@RequestParam(defaultValue = "12") int display) {
        return cacheService.list(display);
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
     * 정당 페이지용. 이것도 라이브 크롤링 대신 캐시에서 조회한다 - 매 진입마다 크롤링+분류를
     * 반복해서 수십 초씩 걸리던 문제(및 소수정당은 그 안에서 매치가 거의 안 나와 비어보이던 문제)를
     * 캐시 풀(최근 3일치, 계속 누적)에서 찾는 방식으로 완화한다.
     * 1순위: 제목에 partyKeyword(정당명)가 그대로 들어있으면 채택.
     * 2순위: leaning(진보/중립/보수/판단불가) 분류 결과가 일치하는 캐시 기사로 채운다.
     */
    @GetMapping("/api/news/by-leaning")
    public List<NewsArticle> byLeaning(
            @RequestParam String leaning,
            @RequestParam(required = false) String partyKeyword,
            @RequestParam(defaultValue = "12") int count
    ) {
        return cacheService.byLeaning(leaning, partyKeyword, count);
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
