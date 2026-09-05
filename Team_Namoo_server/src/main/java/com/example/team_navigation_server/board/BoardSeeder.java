package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Party;
import com.example.team_navigation_server.member.PartyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 정당(활성) x 게시판(1~5) = 25행을 없으면 채운다. data.sql 대신 여기서 하는 이유는 parties 가
 * data.sql 로 먼저 시드되고 나서(같은 트랜잭션 타이밍 이슈 없이) 그 id 를 확실히 참조하기 위함,
 * 그리고 재시작마다 중복 INSERT 되는 parties 시드와 같은 문제를 피하기 위해(existsBy로 가드).
 * 게시판 이름은 아직 확정 전이라 "게시판1~5" 임시 이름을 쓴다 (ADMIN_CONSOLE_BACKEND_TODO.md 5번).
 */
@Component
public class BoardSeeder implements CommandLineRunner {

    private static final int BOARD_COUNT = 5;

    private final PartyRepository partyRepository;
    private final BoardRepository boardRepository;

    public BoardSeeder(PartyRepository partyRepository, BoardRepository boardRepository) {
        this.partyRepository = partyRepository;
        this.boardRepository = boardRepository;
    }

    @Override
    public void run(String... args) {
        for (Party party : partyRepository.findAll()) {
            if (!party.isActive()) {
                continue;
            }
            for (int i = 1; i <= BOARD_COUNT; i++) {
                if (boardRepository.existsByParty_NameAndBoardIndex(party.getName(), i)) {
                    continue;
                }
                boardRepository.save(new Board(party, i, "게시판" + i, false, true));
            }
        }
    }
}
