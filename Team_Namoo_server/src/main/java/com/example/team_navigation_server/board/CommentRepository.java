package com.example.team_navigation_server.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndVisibilityOrderByIdAsc(Post post, PostVisibility visibility);
    long countByPostAndVisibility(Post post, PostVisibility visibility);
}
