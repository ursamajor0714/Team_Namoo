package com.example.team_navigation_server.ad;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {
    List<Ad> findByPageAndSide(String page, AdSide side);
    List<Ad> findAllByOrderByCreatedAtDesc();
    List<Ad> findAllByPageOrderByCreatedAtDesc(String page);
    List<Ad> findByCreatedBy(com.example.team_navigation_server.member.Member createdBy);
}
