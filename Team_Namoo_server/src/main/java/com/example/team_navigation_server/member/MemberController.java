package com.example.team_navigation_server.member;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request){
        memberService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session){
        Member member = memberService.login(request);
        session.setAttribute("loginMemberId", member.getId());
        return ResponseEntity.ok("로그인 성공");
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session){
        Long memberId = (Long) session.getAttribute("loginMemberId");
        if (memberId == null){
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        MemberResponse response = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공");
    }
}
