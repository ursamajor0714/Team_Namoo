package com.example.team_navigation_server.news;

import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 뉴스 원문 링크에 접속해 본문 텍스트만 추출한다.
 * 1순위: Mozilla Readability 알고리즘 포트인 readability4j - 언론사별 셀렉터 없이
 *        본문다운 영역을 자동으로 판별해준다.
 * 2순위(폴백): readability4j가 실패하거나 결과가 너무 짧을 때만, 자체 노이즈 제거 +
 *        셀렉터/링크밀도 휴리스틱 방식으로 대체한다.
 */
@Component
public class ArticleTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(ArticleTextExtractor.class);

    private static final String[] BODY_SELECTORS = {
            "#dic_area",             // 네이버 뉴스
            "#newsct_article",       // 네이버 뉴스(신버전 레이아웃)
            "#articleBodyContents",  // 일부 언론사 공통 템플릿
            "#article-view-content-div",
            ".article_body",
            ".news_end",
            "article"
    };

    /**
     * 본문과 무관한 네비게이션/광고/관련기사/댓글 등 노이즈 요소를 본문 추출 전에 제거한다.
     * 클래스/아이디에 단어 경계(\b)를 사용해 "ad" 같은 짧은 토큰이 "read" 등에 오탐되지 않도록 한다.
     */
    private static final String NOISE_SELECTORS = String.join(",",
            "script", "style", "noscript", "iframe", "form", "button",
            "nav", "header", "footer", "aside",
            // 네이버 뉴스 기사 상단 헤더 블록(제목/기자명/입력·수정일시/기사원문·반응·TTS·글자크기·SNS·인쇄 버튼 등)
            // 제목은 노이즈 제거 전에 별도로 뽑아두므로 통째로 제거해도 안전하다.
            "[id^=media_end_head]", "[class^=media_end_head]",
            "[class~=(?i)media_end_head\\w*]", "[id~=(?i)media_end_head\\w*]",
            "[class~=(?i)\\b(gnb|lnb|snb|nav|navi|navigation|menu|breadcrumb)\\b]",
            "[id~=(?i)\\b(gnb|lnb|snb|nav|navi|navigation|menu|breadcrumb)\\b]",
            "[class~=(?i)\\b(related|relation|recommend|ranking|rank|popular|most)\\b]",
            "[id~=(?i)\\b(related|relation|recommend|ranking|rank|popular|most)\\b]",
            "[class~=(?i)\\b(comment|reply|sns|share|copyright|byline|journalistcard|caption|img_desc|blind|reaction|banner|promotion|ad|ads|adsense|advert)\\b]",
            "[id~=(?i)\\b(comment|reply|sns|share|copyright|byline|journalistcard|caption|img_desc|blind|reaction|banner|promotion|ad|ads|adsense|advert)\\b]"
    );

    private static final int MIN_CANDIDATE_LENGTH = 200;
    private static final int TIMEOUT_MS = 8000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * @param url 기사 원문 링크
     * @return 페이지에서 직접 추출한 제목(title) + 본문(content). 코랩 학습 데이터로 그대로 쓰인다.
     */
    public ExtractedArticle extract(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();

        // 제목은 원본 문서 기준으로 뽑는다 (readability4j/노이즈 제거 전에 먼저 처리)
        String title = extractTitle(doc);

        String content = extractContentWithReadability(url, doc.outerHtml());
        if (content.isBlank()) {
            // readability4j가 실패했거나 결과가 짧으면 자체 방식으로 폴백
            doc.select(NOISE_SELECTORS).remove();
            content = extractContent(doc);
        }

        if (content.isBlank()) {
            // 본문을 전혀 못 뽑으면(예: 자바스크립트로만 렌더링되는 페이지) 빈 결과를 반환하지 않고
            // 예외를 던져서 이 기사는 수집 결과에서 통째로 제외되게 한다.
            throw new IOException("본문 추출 결과가 비어 있음 (JS 렌더링 페이지 등으로 추정): " + url);
        }

        return new ExtractedArticle(title, content);
    }

    private String extractContentWithReadability(String url, String rawHtml) {
        try {
            Readability4J readability4J = new Readability4J(url, rawHtml);
            Article article = readability4J.parse();
            String text = article.getTextContent();
            if (text == null) {
                return "";
            }
            text = text.trim();
            return text.length() >= MIN_CANDIDATE_LENGTH ? text : "";
        } catch (Exception e) {
            log.warn("readability4j 본문 추출 실패, 기존 방식으로 대체: {}", e.getMessage());
            return "";
        }
    }

    /**
     * #title_area(네이버 뉴스 실제 제목 영역) -> og:title -> meta title -> 첫 h1 -> &lt;title&gt; 태그 순으로 시도한다.
     * &lt;title&gt; 태그는 보통 "기사제목 - 언론사명" 형태라 구분자 뒤 언론사명은 잘라낸다.
     */
    private String extractTitle(Document doc) {
        Elements naverTitle = doc.select("#title_area");
        if (!naverTitle.isEmpty()) {
            String text = naverTitle.first().text().trim();
            if (!text.isBlank()) {
                return text;
            }
        }

        String ogTitle = doc.select("meta[property=og:title]").attr("content").trim();
        if (!ogTitle.isBlank()) {
            return ogTitle;
        }

        String metaTitle = doc.select("meta[name=title]").attr("content").trim();
        if (!metaTitle.isBlank()) {
            return metaTitle;
        }

        Elements h1 = doc.select("h1");
        if (!h1.isEmpty()) {
            String h1Text = h1.first().text().trim();
            if (!h1Text.isBlank()) {
                return h1Text;
            }
        }

        String titleTag = doc.title().trim();
        if (!titleTag.isBlank()) {
            return titleTag.split("[-|]")[0].trim();
        }

        return "";
    }

    private String extractContent(Document doc) {
        for (String selector : BODY_SELECTORS) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                String text = elements.text().trim();
                if (text.length() >= MIN_CANDIDATE_LENGTH) {
                    return text;
                }
            }
        }

        String readable = extractByReadabilityHeuristic(doc);
        if (!readable.isBlank()) {
            return readable;
        }

        // 알려진 셀렉터/휴리스틱으로도 못 찾으면 body 전체 텍스트로 대체
        Element body = doc.body();
        return body != null ? body.text().trim() : "";
    }

    public record ExtractedArticle(String title, String content) {
    }

    /**
     * 지정 셀렉터로 본문을 찾지 못한 사이트를 위한 대체 전략.
     * 후보 요소들을 텍스트 길이 * (1 - 링크밀도)로 점수화해 가장 "본문다운" 요소를 고른다.
     * 링크밀도가 높은 블록(메뉴, 관련기사 리스트 등)은 자연히 낮은 점수를 받는다.
     */
    private String extractByReadabilityHeuristic(Document doc) {
        Elements candidates = doc.select(
                "article, main, [class*=article], [class*=content], [id*=article], [id*=content], section, div");

        Element best = null;
        double bestScore = 0;

        for (Element candidate : candidates) {
            String text = candidate.text().trim();
            int textLength = text.length();
            if (textLength < MIN_CANDIDATE_LENGTH) {
                continue;
            }

            int linkTextLength = 0;
            for (Element link : candidate.select("a")) {
                linkTextLength += link.text().length();
            }
            double linkDensity = (double) linkTextLength / textLength;
            if (linkDensity > 0.5) {
                continue;
            }

            double score = textLength * (1 - linkDensity);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best != null ? best.text().trim() : "";
    }
}
