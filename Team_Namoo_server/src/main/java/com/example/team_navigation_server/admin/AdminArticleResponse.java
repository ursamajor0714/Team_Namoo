package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.news.ArticleVisibility;
import com.example.team_navigation_server.news.CachedNewsArticle;

public class AdminArticleResponse {
    private final Long id;
    private final ArticleVisibility visibility;
    private final String title;
    private final String link;
    private final String originalLink;
    private final String leaning;

    public AdminArticleResponse(CachedNewsArticle article) {
        this.id = article.getId();
        this.visibility = article.getVisibility();
        this.title = article.getTitle();
        this.link = article.getLink();
        this.originalLink = article.getOriginalLink();
        this.leaning = article.getLeaning() == null ? null : article.getLeaning().getLabel();
    }

    public Long getId() { return id; }
    public ArticleVisibility getVisibility() { return visibility; }
    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getOriginalLink() { return originalLink; }
    public String getLeaning() { return leaning; }
}
