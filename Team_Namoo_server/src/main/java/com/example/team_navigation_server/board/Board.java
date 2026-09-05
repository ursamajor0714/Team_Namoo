package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Party;
import jakarta.persistence.*;

/**
 * 정당(party) x 게시판 번호(1~5) 조합 하나. 프론트 라우팅 /party/:name/board/:boardId 의
 * boardId 가 이 boardIndex 다 (Board.id 와는 다른 값).
 * 정당 5 x 게시판 5 = 25행, BoardSeeder 가 기동 시 없으면 채운다.
 */
@Entity
@Table(name = "boards", uniqueConstraints = @UniqueConstraint(columnNames = {"party_id", "board_index"}))
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Column(name = "board_index", nullable = false)
    private int boardIndex;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean loginRequired;

    @Column(nullable = false)
    private boolean allowAnonymous;

    protected Board() {
    }

    public Board(Party party, int boardIndex, String name, boolean loginRequired, boolean allowAnonymous) {
        this.party = party;
        this.boardIndex = boardIndex;
        this.name = name;
        this.loginRequired = loginRequired;
        this.allowAnonymous = allowAnonymous;
    }

    public Long getId() {
        return id;
    }

    public Party getParty() {
        return party;
    }

    public int getBoardIndex() {
        return boardIndex;
    }

    public String getName() {
        return name;
    }

    public boolean isLoginRequired() {
        return loginRequired;
    }

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }
}
