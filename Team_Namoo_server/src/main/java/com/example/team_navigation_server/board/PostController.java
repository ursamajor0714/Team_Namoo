package com.example.team_navigation_server.board;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/parties/{partyName}/boards/{boardId}/posts")
    public ResponseEntity<?> createPost(@PathVariable String partyName,
                                         @PathVariable int boardId,
                                         @Valid @RequestBody PostCreateRequest request,
                                         HttpSession session) {
        Long memberId = requireLogin(session);
        if (memberId == null) {
            return unauthorized();
        }
        Long postId = postService.createPost(partyName, boardId, memberId, request);
        return ResponseEntity.ok(Map.of("id", postId));
    }

    @GetMapping("/parties/{partyName}/boards/{boardId}/posts")
    public ResponseEntity<List<PostResponse>> getPosts(@PathVariable String partyName,
                                                         @PathVariable int boardId) {
        return ResponseEntity.ok(postService.getPosts(partyName, boardId));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                         @Valid @RequestBody PostCreateRequest request,
                                         HttpSession session) {
        Long memberId = requireLogin(session);
        if (memberId == null) {
            return unauthorized();
        }
        postService.updatePost(postId, memberId, request);
        return ResponseEntity.ok("수정 완료");
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, HttpSession session) {
        Long memberId = requireLogin(session);
        if (memberId == null) {
            return unauthorized();
        }
        postService.deletePost(postId, memberId);
        return ResponseEntity.ok("삭제 완료");
    }

    private Long requireLogin(HttpSession session) {
        return (Long) session.getAttribute("loginMemberId");
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(401).body("로그인이 필요합니다.");
    }
}
