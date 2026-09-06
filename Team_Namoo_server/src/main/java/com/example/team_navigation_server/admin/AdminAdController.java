package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.member.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ads")
public class AdminAdController {

    private final AdminAdService adminAdService;

    public AdminAdController(AdminAdService adminAdService) {
        this.adminAdService = adminAdService;
    }

    @GetMapping
    public ResponseEntity<List<AdminAdResponse>> list(@RequestParam(required = false) String page) {
        return ResponseEntity.ok(adminAdService.list(page));
    }

    @PostMapping
    public ResponseEntity<AdminAdResponse> create(@Valid @RequestBody AdminAdCreateRequest request,
                                                   HttpServletRequest httpRequest) {
        Member actingAdmin = (Member) httpRequest.getAttribute("currentAdmin");
        return ResponseEntity.status(HttpStatus.CREATED).body(adminAdService.create(request, actingAdmin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        adminAdService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/image/presign")
    public ResponseEntity<AdminAdImagePresignResponse> presign(@Valid @RequestBody AdminAdImagePresignRequest request) {
        return ResponseEntity.ok(adminAdService.presignImageUpload(request));
    }
}
