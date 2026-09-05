package com.example.team_navigation_server.config;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberRepository;
import com.example.team_navigation_server.member.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * /api/admin/** 전체를 "로그인 + role ADMIN 이상"만 통과시킨다.
 * 비로그인은 401, 로그인했지만 권한 부족은 403.
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;

    public AdminAuthInterceptor(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        Long memberId = session == null ? null : (Long) session.getAttribute("loginMemberId");
        if (memberId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }

        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null || member.getRole() == MemberRole.USER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
            return false;
        }

        request.setAttribute("currentAdmin", member);
        return true;
    }
}
