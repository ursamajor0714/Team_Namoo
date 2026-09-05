package com.example.team_navigation_server.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByParty_NameAndBoardIndex(String partyName, int boardIndex);
    boolean existsByParty_NameAndBoardIndex(String partyName, int boardIndex);
}
