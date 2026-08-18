package com.example.team_navigation_server.news;

import org.springframework.web.bind.annotation.GetMapping;
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

    private final NewsCollectionService collectionService;
    private final NewsExportService exportService;

    public NewsController(NewsCollectionService collectionService, NewsExportService exportService) {
        this.collectionService = collectionService;
        this.exportService = exportService;
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
