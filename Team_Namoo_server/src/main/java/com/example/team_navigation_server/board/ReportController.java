package com.example.team_navigation_server.board;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/posts/{postId}/report")
    public ResponseEntity<?> reportPost(@PathVariable Long postId,
                                         @Valid @RequestBody ReportCreateRequest request,
                                         HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        reportService.reportPost(postId, memberId, request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/comments/{commentId}/report")
    public ResponseEntity<?> reportComment(@PathVariable Long commentId,
                                            @Valid @RequestBody ReportCreateRequest request,
                                            HttpSession session) {
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        reportService.reportComment(commentId, memberId, request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
