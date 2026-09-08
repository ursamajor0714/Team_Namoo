package com.example.team_navigation_server.board;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/parties/{partyName}/boards/{boardId}/posts")
    public ResponseEntity<PostListResponse> list(@PathVariable String partyName,
                                                  @PathVariable int boardId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.list(partyName, boardId, page, size));
    }

    @PostMapping("/api/parties/{partyName}/boards/{boardId}/posts")
    public ResponseEntity<PostDetailResponse> create(@PathVariable String partyName,
                                                      @PathVariable int boardId,
                                                      @Valid @RequestBody PostCreateRequest request,
                                                      HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        PostDetailResponse response = postService.create(partyName, boardId, memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/posts/{postId}")
    public ResponseEntity<PostDetailResponse> getDetail(@PathVariable Long postId, HttpSession session) {
        return ResponseEntity.ok(postService.getDetail(postId, (Long) session.getAttribute("loginMemberId")));
    }

    @PutMapping("/api/posts/{postId}")
    public ResponseEntity<?> update(@PathVariable Long postId,
                                     @Valid @RequestBody PostUpdateRequest request,
                                     HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(postService.update(postId, memberId, request));
    }

    @DeleteMapping("/api/posts/{postId}")
    public ResponseEntity<?> delete(@PathVariable Long postId, HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        postService.delete(postId, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/posts/{postId}/vote")
    public ResponseEntity<?> vote(@PathVariable Long postId,
                                   @Valid @RequestBody PostVoteRequest request,
                                   HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(postService.vote(postId, memberId, request));
    }

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId, HttpSession session) {
        return ResponseEntity.ok(postService.getComments(postId, (Long) session.getAttribute("loginMemberId")));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long postId,
                                                          @Valid @RequestBody CommentCreateRequest request,
                                                          HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        CommentResponse response = postService.createComment(postId, memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                            @Valid @RequestBody CommentUpdateRequest request,
                                            HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(postService.updateComment(commentId, memberId, request));
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        postService.deleteComment(commentId, memberId);
        return ResponseEntity.ok().build();
    }
}
