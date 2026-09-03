package com.example.team_navigation_server.news;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CachedNewsArticleRepository extends JpaRepository<CachedNewsArticle, Long> {

    boolean existsByOriginalLink(String originalLink);

    List<CachedNewsArticle> findByCollectedAtAfterOrderByCollectedAtDesc(Instant cutoff);

    long deleteByCollectedAtBefore(Instant cutoff);
}
