package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.member.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminMemberResponse>> search(@RequestParam(required = false) String field,
                                                             @RequestParam(required = false) String q,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminMemberService.search(field, q, page, size));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(adminMemberService.stats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminMemberResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminMemberService.getDetail(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AdminMemberUpdateRequest request) {
        adminMemberService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminMemberStatusRequest request) {
        adminMemberService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                         @Valid @RequestBody AdminMemberRoleRequest request,
                                         HttpServletRequest httpRequest) {
        Member actingAdmin = (Member) httpRequest.getAttribute("currentAdmin");
        adminMemberService.updateRole(id, request.getRole(), actingAdmin);
        return ResponseEntity.ok().build();
    }
}
