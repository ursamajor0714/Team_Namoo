package com.example.team_navigation_server.news;

import com.example.team_navigation_server.classification.ClassificationModelClient;
import com.example.team_navigation_server.classification.ClassificationResult;
import com.example.team_navigation_server.classification.PoliticalLeaning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
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
    private final ArticleTextExtractor articleTextExtractor;

    public NewsController(NewsCollectionService collectionService, NewsExportService exportService,
                           ClassificationModelClient classificationModelClient, NewsCacheService cacheService,
                          ArticleTextExtractor articleTextExtractor) {
        this.collectionService = collectionService;
        this.exportService = exportService;
        this.classificationModelClient = classificationModelClient;
        this.cacheService = cacheService;
        this.articleTextExtractor = articleTextExtractor;
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
     * 기사 하나를 분류 모델에 넣어 성향을 받아온다. 로컬 분류 모델(classification-api)만 호출한다 -
     * Claude API 키가 없어도 동작한다. 뉴스 모달 하단 태그와 AI 체험 페이지(/ai)가 함께 쓴다.
     * 분류 서버가 안 떠 있거나 실패하면 leaning=null 을 돌려주고, 부르는 쪽이 알아서 처리한다
     * (모달은 태그를 안 보여주고, 체험 페이지는 안내 문구를 띄운다).
     * confidence 는 모델이 그 판정에 준 확률을 백분율로 반올림한 값이다.
     */
    @PostMapping("/api/news/classify")
    public ClassifyResponse classify(@RequestBody ClassifyRequest request) {
        String title = request.title();
        String content = request.content();

        // url 이 오면 그 주소의 기사를 직접 읽어와 제목/본문을 채운다(체험 페이지의 '링크로 판단').
        if (request.url() != null && !request.url().isBlank()) {
            try {
                assertFetchableUrl(request.url());
                ArticleTextExtractor.ExtractedArticle extracted = articleTextExtractor.extract(request.url().trim());
                title = extracted.title();
                content = extracted.content();
            } catch (IllegalArgumentException e) {
                return ClassifyResponse.error(e.getMessage());
            } catch (Exception e) {
                log.warn("링크 본문 추출 실패 {}: {}", request.url(), e.getMessage());
                return ClassifyResponse.error("이 주소에서 기사 본문을 읽지 못했습니다. 본문을 직접 붙여넣어 주세요.");
            }
            if (content == null || content.isBlank()) {
                return ClassifyResponse.error("이 주소에서 기사 본문을 찾지 못했습니다. 본문을 직접 붙여넣어 주세요.");
            }
        }

        try {
            ClassificationResult result = classificationModelClient.classifyWithConfidence(title, content);
            int percent = (int) Math.round(result.confidence() * 100);
            return new ClassifyResponse(result.leaning().getLabel(), percent, title, null);
        } catch (Exception e) {
            log.warn("기사 분류 실패(분류 서버 미기동 등): {}", e.getMessage());
            return new ClassifyResponse(null, 0, title, null);
        }
    }

    /**
     * 사용자가 준 주소를 서버가 대신 열어보는 구조라, 외부 웹 문서인지 먼저 확인한다.
     * http/https 가 아니거나 내부망·루프백·클라우드 메타데이터 주소를 가리키면 거부한다(SSRF 방지).
     */
    private void assertFetchableUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("올바른 주소가 아닙니다.");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("http 또는 https 로 시작하는 기사 주소를 넣어주세요.");
        }
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("올바른 주소가 아닙니다.");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IllegalArgumentException("이 주소는 열어볼 수 없습니다.");
                }
            }
        } catch (java.net.UnknownHostException e) {
            throw new IllegalArgumentException("주소를 찾을 수 없습니다.");
        }
    }

    /** url 을 주면 그 주소를 읽어 분류하고, 없으면 title/content 를 그대로 분류한다. */
    public record ClassifyRequest(String title, String content, String url) {
    }

    /**
     * leaning 이 null 이면 판정하지 못한 것이다. message 가 있으면 그 사유를 사람이 읽을 문구로 담는다.
     * title 은 링크로 요청했을 때 서버가 실제로 읽어온 기사 제목이다(무엇을 읽었는지 화면에 보여주려고).
     */
    public record ClassifyResponse(String leaning, int confidence, String title, String message) {
        static ClassifyResponse error(String message) {
            return new ClassifyResponse(null, 0, null, message);
        }
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
