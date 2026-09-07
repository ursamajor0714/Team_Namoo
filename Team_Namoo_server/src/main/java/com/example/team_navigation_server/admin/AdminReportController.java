package com.example.team_navigation_server.admin;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping("/api/admin/reports")
    public ResponseEntity<Page<AdminReportResponse>> search(@RequestParam(required = false) String status,
                                                              @RequestParam(required = false) String targetType,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminReportService.search(status, targetType, page, size));
    }

    @PatchMapping("/api/admin/reports/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                           @Valid @RequestBody AdminReportStatusRequest request) {
        adminReportService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }
}
