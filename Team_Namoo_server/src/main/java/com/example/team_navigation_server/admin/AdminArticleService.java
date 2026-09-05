package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.classification.PoliticalLeaning;
import com.example.team_navigation_server.news.ArticleVisibility;
import com.example.team_navigation_server.news.CachedNewsArticle;
import com.example.team_navigation_server.news.CachedNewsArticleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminArticleService {

    // Team_Namoo_Front/src/constants/parties.js 의 PARTY_LEANING과 동일하게 맞춘다.
    private static final Map<String, PoliticalLeaning> PARTY_LEANING = Map.of(
            "더불어민주당", PoliticalLeaning.PROGRESSIVE,
            "국민의힘", PoliticalLeaning.CONSERVATIVE,
            "조국혁신당", PoliticalLeaning.PROGRESSIVE,
            "진보당", PoliticalLeaning.PROGRESSIVE,
            "개혁신당", PoliticalLeaning.CONSERVATIVE
    );

    private final CachedNewsArticleRepository repository;

    public AdminArticleService(CachedNewsArticleRepository repository) {
        this.repository = repository;
    }

    public List<AdminArticleResponse> search(String party, String scope, String q) {
        List<CachedNewsArticle> articles = repository.findAllByOrderByCollectedAtDesc();

        if (party != null && !party.isBlank()) {
            PoliticalLeaning leaning = PARTY_LEANING.get(party);
            if (leaning == null) {
                throw new IllegalArgumentException("지원하지 않는 정당입니다.");
            }
            articles = articles.stream().filter(a -> a.getLeaning() == leaning).toList();
        }

        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            articles = articles.stream().filter(a -> switch (scope == null ? "title" : scope) {
                case "content" -> a.getContent() != null && a.getContent().toLowerCase().contains(needle);
                case "title" -> a.getTitle() != null && a.getTitle().toLowerCase().contains(needle);
                default -> throw new IllegalArgumentException("검색 범위는 title/content 중 하나여야 합니다.");
            }).toList();
        }

        return articles.stream().map(AdminArticleResponse::new).toList();
    }

    public void updateVisibility(Long id, String visibilityValue) {
        CachedNewsArticle article = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기사입니다."));
        article.setVisibility(parseVisibility(visibilityValue));
        repository.save(article);
    }

    public void updateVisibilityBulk(List<Long> ids, String visibilityValue) {
        ArticleVisibility visibility = parseVisibility(visibilityValue);
        List<CachedNewsArticle> articles = repository.findAllById(ids);
        articles.forEach(a -> a.setVisibility(visibility));
        repository.saveAll(articles);
    }

    private ArticleVisibility parseVisibility(String value) {
        try {
            return ArticleVisibility.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("노출 상태는 NORMAL/HIDDEN/DELETED 중 하나여야 합니다.");
        }
    }
}
