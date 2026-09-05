package com.example.team_navigation_server.news;

import com.example.team_navigation_server.classification.PoliticalLeaning;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 정당 페이지/뉴스 목록에 쓰이는 뉴스 캐시.
 * 백그라운드 스케줄러(NewsCacheRefreshScheduler)가 주기적으로 네이버에서 최신 정치 기사를
 * 수집 -> 분류해서 여기 저장해두고, 컨트롤러는 매 요청마다 라이브 크롤링하는 대신
 * NewsCacheService를 통해 이 캐시를 조회만 한다. collectedAt 기준 최근 3일치만 유지하고
 * 그보다 오래된 건 주기적으로 지운다(NewsCacheService.pruneOld).
 * leaning은 분류 서버가 안 떠 있거나 실패하면 null일 수 있다.
 */
@Entity
@Table(name = "cached_news_articles")
public class CachedNewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2000)
    private String originalLink;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String link;

    @Lob
    private String description;

    private String pubDate;

    @Lob
    private String content;

    @Lob
    private String contentHtml;

    @Lob
    private String summary;

    @Column(length = 2000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private PoliticalLeaning leaning;

    @Column(nullable = false)
    private Instant collectedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleVisibility visibility = ArticleVisibility.NORMAL;

    protected CachedNewsArticle() {
    }

    public CachedNewsArticle(String originalLink, String title, String link, String description, String pubDate,
                              String content, String contentHtml, String summary, String imageUrl,
                              PoliticalLeaning leaning, Instant collectedAt) {
        this.originalLink = originalLink;
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
        this.content = content;
        this.contentHtml = contentHtml;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.leaning = leaning;
        this.collectedAt = collectedAt;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getContent() {
        return content;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public String getSummary() {
        return summary;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public PoliticalLeaning getLeaning() {
        return leaning;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public ArticleVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ArticleVisibility visibility) {
        this.visibility = visibility;
    }
}
