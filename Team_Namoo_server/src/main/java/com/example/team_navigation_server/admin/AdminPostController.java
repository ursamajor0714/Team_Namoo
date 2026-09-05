package com.example.team_navigation_server.admin;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminPostController {

    private final AdminPostService adminPostService;

    public AdminPostController(AdminPostService adminPostService) {
        this.adminPostService = adminPostService;
    }

    @GetMapping("/api/admin/parties/{partyName}/boards/{boardId}/posts")
    public ResponseEntity<List<AdminPostResponse>> search(@PathVariable String partyName,
                                                           @PathVariable int boardId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPostService.search(partyName, boardId, page, size));
    }

    @PatchMapping("/api/admin/posts/{id}/visibility")
    public ResponseEntity<?> updateVisibility(@PathVariable Long id,
                                               @Valid @RequestBody AdminPostVisibilityRequest request) {
        adminPostService.updateVisibility(id, request.getVisibility());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/admin/posts/visibility")
    public ResponseEntity<?> updateVisibilityBulk(@Valid @RequestBody AdminPostBulkVisibilityRequest request) {
        adminPostService.updateVisibilityBulk(request.getIds(), request.getVisibility());
        return ResponseEntity.ok().build();
    }
}
