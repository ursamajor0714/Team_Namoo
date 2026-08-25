package com.example.team_navigation_server.classification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 분류 결과 저장용 엔티티. 정치성향 분류 테스트 파이프라인 전용 - 확인 끝나면 이 패키지 전체를 삭제할 예정.
 */
@Entity
@Table(name = "news_classifications")
public class NewsClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String originalLink;

    @Column(columnDefinition = "CLOB")
    private String cleanedContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PoliticalLeaning leaning;

    @Column(columnDefinition = "CLOB")
    private String frontendBlurb;

    protected NewsClassification() {
    }

    public NewsClassification(String title, String originalLink, String cleanedContent,
                               PoliticalLeaning leaning, String frontendBlurb) {
        this.title = title;
        this.originalLink = originalLink;
        this.cleanedContent = cleanedContent;
        this.leaning = leaning;
        this.frontendBlurb = frontendBlurb;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public String getCleanedContent() {
        return cleanedContent;
    }

    public PoliticalLeaning getLeaning() {
        return leaning;
    }

    public String getFrontendBlurb() {
        return frontendBlurb;
    }
}
