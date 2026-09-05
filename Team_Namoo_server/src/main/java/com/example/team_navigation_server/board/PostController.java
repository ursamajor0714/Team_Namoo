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
    public ResponseEntity<PostDetailResponse> getDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getDetail(postId));
    }

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getComments(postId));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long postId,
                                                          @Valid @RequestBody CommentCreateRequest request,
                                                          HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        CommentResponse response = postService.createComment(postId, memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
