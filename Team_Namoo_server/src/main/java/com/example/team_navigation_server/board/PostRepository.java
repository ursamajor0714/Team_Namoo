package com.example.team_navigation_server.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByParty_IdAndBoardIdOrderByCreatedAtDesc(Long partyId, int boardId);
}
