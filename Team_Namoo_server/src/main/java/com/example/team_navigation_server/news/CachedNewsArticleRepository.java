package com.example.team_navigation_server.news;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CachedNewsArticleRepository extends JpaRepository<CachedNewsArticle, Long> {

    boolean existsByOriginalLink(String originalLink);

    List<CachedNewsArticle> findByCollectedAtAfterAndVisibilityOrderByCollectedAtDesc(
            Instant cutoff, ArticleVisibility visibility);

    List<CachedNewsArticle> findAllByOrderByCollectedAtDesc();

    long deleteByCollectedAtBefore(Instant cutoff);
}
