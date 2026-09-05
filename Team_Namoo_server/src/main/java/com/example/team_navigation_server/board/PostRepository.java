package com.example.team_navigation_server.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 회원용 - 고정(공지) 목록, 최신순, visibility 필터
    java.util.List<Post> findByBoardAndPinnedTrueAndVisibilityOrderByIdDesc(Board board, PostVisibility visibility);

    // 회원용 - 일반 글 페이지, visibility 필터
    Page<Post> findByBoardAndPinnedFalseAndVisibility(Board board, PostVisibility visibility, Pageable pageable);

    long countByBoardAndPinnedFalseAndVisibility(Board board, PostVisibility visibility);

    // 관리자용 - 상태/고정 무관 전체
    Page<Post> findByBoard(Board board, Pageable pageable);

    long countByBoard(Board board);
}
