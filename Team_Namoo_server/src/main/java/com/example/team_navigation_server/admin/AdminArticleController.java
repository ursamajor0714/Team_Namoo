package com.example.team_navigation_server.admin;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    private final AdminArticleService adminArticleService;

    public AdminArticleController(AdminArticleService adminArticleService) {
        this.adminArticleService = adminArticleService;
    }

    @GetMapping
    public ResponseEntity<List<AdminArticleResponse>> search(@RequestParam(required = false) String party,
                                                              @RequestParam(required = false) String scope,
                                                              @RequestParam(required = false) String q) {
        return ResponseEntity.ok(adminArticleService.search(party, scope, q));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<?> updateVisibility(@PathVariable Long id,
                                               @Valid @RequestBody AdminArticleVisibilityRequest request) {
        adminArticleService.updateVisibility(id, request.getVisibility());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/visibility")
    public ResponseEntity<?> updateVisibilityBulk(@Valid @RequestBody AdminArticleBulkVisibilityRequest request) {
        adminArticleService.updateVisibilityBulk(request.getIds(), request.getVisibility());
        return ResponseEntity.ok().build();
    }
}
