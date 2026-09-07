package com.example.team_navigation_server.config;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberRepository;
import com.example.team_navigation_server.member.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
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
        // 브라우저는 POST/PATCH 전에 CORS 예비 요청(OPTIONS)을 먼저 보내는데 여기엔 세션 쿠키가 실리지 않는다.
        // 이걸 401로 막으면 브라우저가 본 요청을 아예 보내지 않아 프론트에는 "네트워크 에러"만 보인다.
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

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
