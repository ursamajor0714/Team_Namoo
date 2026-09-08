package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberService memberService;
    private final PostVoteRepository postVoteRepository;

    public PostService(BoardRepository boardRepository, PostRepository postRepository,
                        CommentRepository commentRepository, MemberService memberService,
                        PostVoteRepository postVoteRepository) {
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.memberService = memberService;
        this.postVoteRepository = postVoteRepository;
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
        return new PostDetailResponse(post, 0, memberId);
    }

    @Transactional
    public PostDetailResponse getDetail(Long postId, Long viewerMemberId) {
        Post post = findVisiblePost(postId);
        post.increaseViews();
        long commentCount = commentCount(post);
        return new PostDetailResponse(post, commentCount, viewerMemberId);
    }

    public List<CommentResponse> getComments(Long postId, Long viewerMemberId) {
        Post post = findVisiblePost(postId);
        return commentRepository.findByPostAndVisibilityOrderByIdAsc(post, PostVisibility.NORMAL)
                .stream()
                .map(comment -> new CommentResponse(comment, viewerMemberId))
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long postId, Long memberId, CommentCreateRequest request) {
        Post post = findVisiblePost(postId);
        Member member = resolveWriter(post.getBoard(), memberId);
        String authorName = member != null ? member.getNickname() : "익명";

        Comment comment = new Comment(post, member, authorName, request.getContent());
        commentRepository.save(comment);
        return new CommentResponse(comment, memberId);
    }

    // 본인 글 수정 - 익명 글(authorMember 없음)은 소유자 특정이 안 돼서 수정 대상이 아니다.
    @Transactional
    public PostDetailResponse update(Long postId, Long memberId, PostUpdateRequest request) {
        Post post = findVisiblePost(postId);
        requireOwner(post.getAuthorMember(), memberId);
        post.updateContent(request.getTitle(), request.getContent());
        return new PostDetailResponse(post, commentCount(post), memberId);
    }

    // 본인 글 삭제 - 관리자 삭제와 동일하게 실제로 안 지우고 visibility만 DELETED로(복구 가능, 신고 이력 등 유지)
    @Transactional
    public void delete(Long postId, Long memberId) {
        Post post = findVisiblePost(postId);
        requireOwner(post.getAuthorMember(), memberId);
        post.setVisibility(PostVisibility.DELETED);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long memberId, CommentUpdateRequest request) {
        Comment comment = findVisibleComment(commentId);
        requireOwner(comment.getAuthorMember(), memberId);
        comment.updateContent(request.getContent());
        return new CommentResponse(comment, memberId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = findVisibleComment(commentId);
        requireOwner(comment.getAuthorMember(), memberId);
        comment.setVisibility(PostVisibility.DELETED);
    }

    // 로그인 상태(활성 계정)이면서 실제 작성자 본인인지 확인 - 익명 글/댓글(author null)은 항상 거부.
    private void requireOwner(Member author, Long memberId) {
        memberService.requireActiveMember(memberId);
        if (author == null || !author.getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 글/댓글만 수정·삭제할 수 있습니다.");
        }
    }

    private Comment findVisibleComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (comment.getVisibility() != PostVisibility.NORMAL) {
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        }
        return comment;
    }

    // 추천/비추천 - 로그인 회원만 가능(익명은 식별 불가). 같은 타입 재클릭 시 취소, 다른 타입이면 전환.
    @Transactional
    public PostVoteResponse vote(Long postId, Long memberId, PostVoteRequest request) {
        Post post = findVisiblePost(postId);
        Member member = memberService.requireActiveMember(memberId);
        PostVoteType type = parseVoteType(request.getType());

        Optional<PostVote> existing = postVoteRepository.findByPostAndMember(post, member);
        String myVote;
        if (existing.isEmpty()) {
            postVoteRepository.save(new PostVote(post, member, type));
            applyVoteDelta(post, type, 1);
            myVote = type.name();
        } else {
            PostVote vote = existing.get();
            if (vote.getType() == type) {
                postVoteRepository.delete(vote);
                applyVoteDelta(post, type, -1);
                myVote = null;
            } else {
                applyVoteDelta(post, vote.getType(), -1);
                applyVoteDelta(post, type, 1);
                vote.changeType(type);
                myVote = type.name();
            }
        }
        return new PostVoteResponse(post.getLikes(), post.getDislikes(), myVote);
    }

    private void applyVoteDelta(Post post, PostVoteType type, int delta) {
        if (type == PostVoteType.LIKE) {
            if (delta > 0) post.increaseLikes(); else post.decreaseLikes();
        } else {
            if (delta > 0) post.increaseDislikes(); else post.decreaseDislikes();
        }
    }

    private PostVoteType parseVoteType(String raw) {
        try {
            return PostVoteType.valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("잘못된 추천 유형입니다.");
        }
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
        Member member = memberService.requireActiveMember(memberId);
        if (member.getSupportedParty() == null || !member.getSupportedParty().getId().equals(board.getParty().getId())) {
            throw new IllegalArgumentException("지지 정당으로 설정한 게시판에서만 글/댓글을 쓸 수 있습니다.");
        }
        return member;
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
