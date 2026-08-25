package com.example.team_navigation_server.classification;

import com.example.team_navigation_server.news.NewsArticle;
import com.example.team_navigation_server.news.NewsCollectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 예) GET /api/classification/test?query=정치&count=3
 * 네이버 수집 -> 클로드 중복거르기/정리 -> (목업)분류 -> DB저장까지 한 번에 확인하는 테스트 엔드포인트.
 * GET /api/classification/results 로 저장된 결과를 다시 조회할 수 있다.
 * 검증 끝나면 이 패키지 전체를 삭제할 예정.
 */
@RestController
public class NewsClassificationController {

    private final NewsCollectionService collectionService;
    private final NewsClassificationService classificationService;
    private final NewsClassificationRepository repository;

    public NewsClassificationController(NewsCollectionService collectionService,
                                         NewsClassificationService classificationService,
                                         NewsClassificationRepository repository) {
        this.collectionService = collectionService;
        this.classificationService = classificationService;
        this.repository = repository;
    }

    @GetMapping("/api/classification/test")
    public List<NewsClassification> testPipeline(
            @RequestParam(defaultValue = "정치") String query,
            @RequestParam(defaultValue = "3") int count
    ) throws IOException {
        List<NewsArticle> articles = collectionService.collect(query, count, 1, "sim", false);
        return classificationService.classifyAndSave(articles);
    }

    @GetMapping("/api/classification/results")
    public List<NewsClassification> results() {
        return repository.findAll();
    }
}
