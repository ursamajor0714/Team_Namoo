package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    public PostService(BoardRepository boardRepository, PostRepository postRepository,
                        CommentRepository commentRepository, MemberRepository memberRepository) {
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
    }

    public PostListResponse list(String partyName, int boardIndex, int page, int size) {
        Board board = findBoard(partyName, boardIndex);

        List<PostSummaryResponse> notices = postRepository
                .findByBoardAndPinnedTrueAndVisibilityOrderByIdDesc(board, PostVisibility.NORMAL)
                .stream()
                .map(post -> new PostSummaryResponse(post, null, commentCount(post)))
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Post> pageResult = postRepository.findByBoardAndPinnedFalseAndVisibility(board, PostVisibility.NORMAL, pageable);
        long totalCount = postRepository.countByBoardAndPinnedFalseAndVisibility(board, PostVisibility.NORMAL);

        long offset = (long) page * size;
        List<Post> content = pageResult.getContent();
        List<PostSummaryResponse> posts = new ArrayList<>(content.size());
        for (int i = 0; i < content.size(); i++) {
            Post post = content.get(i);
            int num = (int) (totalCount - offset - i);
            posts.add(new PostSummaryResponse(post, num, commentCount(post)));
        }

        return new PostListResponse(notices, posts, totalCount);
    }

    @Transactional
    public PostDetailResponse create(String partyName, int boardIndex, Long memberId, PostCreateRequest request) {
        Board board = findBoard(partyName, boardIndex);
        Member member = resolveWriter(board, memberId);
        String authorName = member != null ? member.getNickname() : "익명";

        Post post = new Post(board, member, authorName, request.getTitle(), request.getContent());
        postRepository.save(post);
        return new PostDetailResponse(post, 0);
    }

    @Transactional
    public PostDetailResponse getDetail(Long postId) {
        Post post = findVisiblePost(postId);
        post.increaseViews();
        long commentCount = commentCount(post);
        return new PostDetailResponse(post, commentCount);
    }

    public List<CommentResponse> getComments(Long postId) {
        Post post = findVisiblePost(postId);
        return commentRepository.findByPostAndVisibilityOrderByIdAsc(post, PostVisibility.NORMAL)
                .stream()
                .map(CommentResponse::new)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long postId, Long memberId, CommentCreateRequest request) {
        Post post = findVisiblePost(postId);
        Member member = resolveWriter(post.getBoard(), memberId);
        String authorName = member != null ? member.getNickname() : "익명";

        Comment comment = new Comment(post, member, authorName, request.getContent());
        commentRepository.save(comment);
        return new CommentResponse(comment);
    }

    private Member resolveWriter(Board board, Long memberId) {
        if (memberId == null) {
            if (board.isLoginRequired()) {
                throw new IllegalArgumentException("로그인이 필요한 게시판입니다.");
            }
            if (!board.isAllowAnonymous()) {
                throw new IllegalArgumentException("비로그인 글쓰기가 허용되지 않는 게시판입니다.");
            }
            return null;
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private Board findBoard(String partyName, int boardIndex) {
        return boardRepository.findByParty_NameAndBoardIndex(partyName, boardIndex)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다."));
    }

    private Post findVisiblePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        if (post.getVisibility() != PostVisibility.NORMAL) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
        return post;
    }

    private long commentCount(Post post) {
        return commentRepository.countByPostAndVisibility(post, PostVisibility.NORMAL);
    }
}
