package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.board.Board;
import com.example.team_navigation_server.board.BoardRepository;
import com.example.team_navigation_server.board.Post;
import com.example.team_navigation_server.board.PostRepository;
import com.example.team_navigation_server.board.PostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminPostService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;

    public AdminPostService(BoardRepository boardRepository, PostRepository postRepository) {
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
    }

    public List<AdminPostResponse> search(String partyName, int boardIndex, int page, int size) {
        Board board = boardRepository.findByParty_NameAndBoardIndex(partyName, boardIndex)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다."));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Post> pageResult = postRepository.findByBoard(board, pageable);
        long totalCount = postRepository.countByBoard(board);
        long offset = (long) page * size;

        List<Post> content = pageResult.getContent();
        List<AdminPostResponse> result = new ArrayList<>(content.size());
        for (int i = 0; i < content.size(); i++) {
            int num = (int) (totalCount - offset - i);
            result.add(new AdminPostResponse(content.get(i), num));
        }
        return result;
    }

    public void updateVisibility(Long id, String visibilityValue) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.setVisibility(parseVisibility(visibilityValue));
        postRepository.save(post);
    }

    public void updateVisibilityBulk(List<Long> ids, String visibilityValue) {
        PostVisibility visibility = parseVisibility(visibilityValue);
        List<Post> posts = postRepository.findAllById(ids);
        posts.forEach(p -> p.setVisibility(visibility));
        postRepository.saveAll(posts);
    }

    private PostVisibility parseVisibility(String value) {
        try {
            return PostVisibility.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("노출 상태는 NORMAL/HIDDEN/DELETED 중 하나여야 합니다.");
        }
    }
}
