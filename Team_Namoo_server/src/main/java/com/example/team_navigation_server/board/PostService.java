package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberRepository;
import com.example.team_navigation_server.member.Party;
import com.example.team_navigation_server.member.PartyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, PartyRepository partyRepository,
                        MemberRepository memberRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.partyRepository = partyRepository;
        this.memberRepository = memberRepository;
        this.commentRepository = commentRepository;
    }

    // 게시글 작성
    public Long createPost(String partyName, int boardId, Long memberId, PostCreateRequest request) {
        Party party = partyRepository.findByName(partyName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정당입니다."));
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Post post = new Post(party, boardId, request.getTitle(), request.getContent(), author);
        postRepository.save(post);
        return post.getId();
    }

    // 게시판 목록 조회
    @Transactional(readOnly = true)
    public List<PostResponse> getPosts(String partyName, int boardId) {
        Party party = partyRepository.findByName(partyName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정당입니다."));

        return postRepository.findByParty_IdAndBoardIdOrderByCreatedAtDesc(party.getId(), boardId)
                .stream()
                .map(post -> new PostResponse(post, commentRepository.countByPost_Id(post.getId())))
                .toList();
    }

    // 게시글 상세 조회 (조회수 +1)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.increaseViewCount();
        return new PostResponse(post, commentRepository.countByPost_Id(postId));
    }

    // 게시글 수정 (작성자 본인만)
    public void updatePost(Long postId, Long memberId, PostCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        validateAuthor(post, memberId);
        post.update(request.getTitle(), request.getContent());
    }

    // 게시글 삭제 (작성자 본인만)
    public void deletePost(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        validateAuthor(post, memberId);
        postRepository.delete(post);
    }

    private void validateAuthor(Post post, Long memberId) {
        if (!post.getAuthor().getId().equals(memberId)) {
            throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
        }
    }
}
