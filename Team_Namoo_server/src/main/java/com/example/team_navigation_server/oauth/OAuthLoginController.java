package com.example.team_navigation_server.oauth;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberResponse;
import com.example.team_navigation_server.member.MemberService;
import com.example.team_navigation_server.member.OAuthProvider;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 구글/네이버 SNS 로그인. 기존 세션 로그인(MemberController)과 동일하게
 * HttpSession의 "loginMemberId" 로 로그인 상태를 유지한다 - Spring Security는 안 씀.
 *
 * 흐름:
 *  1) GET  /api/members/oauth/{provider}/login-url  -> 프론트가 이 url로 브라우저를 리다이렉트
 *  2) 사용자가 구글/네이버에서 로그인 동의 -> oauth.{provider}.redirect-uri(프론트 콜백 페이지)로
 *     ?code=...&state=... 를 달고 돌아옴
 *  3) 프론트가 그 code/state를 그대로 POST /api/members/oauth/{provider}/callback 으로 전달
 *     - 이미 연동된(또는 이메일이 같은 기존 계정과 연결 가능한) 회원이면 바로 로그인 처리
 *     - 처음 보는 사용자면 세션에 신원을 잠깐 보관해두고 "SIGNUP_REQUIRED" 응답
 *  4) SIGNUP_REQUIRED 면 프론트가 닉네임/지지정당 등 추가정보 입력받아
 *     POST /api/members/oauth/signup 으로 가입 완료
 */
@RestController
@RequestMapping("/api/members/oauth")
public class OAuthLoginController {

    private static final String SESSION_STATE = "oauthState";
    private static final String SESSION_PENDING_PROVIDER = "oauthPendingProvider";
    private static final String SESSION_PENDING_ID = "oauthPendingId";
    private static final String SESSION_PENDING_EMAIL = "oauthPendingEmail";

    private final MemberService memberService;
    private final Map<String, OAuthClient> clientsByProvider;

    public OAuthLoginController(MemberService memberService, List<OAuthClient> clients) {
        this.memberService = memberService;
        this.clientsByProvider = clients.stream()
                .collect(Collectors.toMap(c -> c.provider().name().toLowerCase(Locale.ROOT), c -> c));
    }

    @GetMapping("/{provider}/login-url")
    public ResponseEntity<OAuthLoginUrlResponse> loginUrl(@PathVariable String provider, HttpSession session) {
        OAuthClient client = resolveClient(provider);
        String state = UUID.randomUUID().toString();
        session.setAttribute(SESSION_STATE, state);
        return ResponseEntity.ok(new OAuthLoginUrlResponse(client.authorizeUrl(state)));
    }

    @PostMapping("/{provider}/callback")
    public ResponseEntity<OAuthCallbackResponse> callback(@PathVariable String provider,
                                                            @Valid @RequestBody OAuthCallbackRequest request,
                                                            HttpSession session) {
        OAuthClient client = resolveClient(provider);

        Object expectedState = session.getAttribute(SESSION_STATE);
        session.removeAttribute(SESSION_STATE);
        if (expectedState == null || !expectedState.equals(request.getState())) {
            throw new IllegalArgumentException("로그인 요청이 만료되었거나 올바르지 않습니다. 다시 시도해주세요.");
        }

        OAuthUserInfo userInfo;
        try {
            userInfo = client.exchange(request.getCode());
        } catch (IOException e) {
            // 만료/재사용된 code, 네트워크 오류 등 - 사용자에게는 다시 시도를 안내하는 400으로 통일
            throw new IllegalArgumentException("SNS 로그인 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
        if (userInfo.getProviderId() == null) {
            throw new IllegalArgumentException("SNS 로그인 정보를 가져오지 못했습니다.");
        }
        if (userInfo.getEmail() == null) {
            throw new IllegalArgumentException("이메일 제공에 동의해야 로그인할 수 있습니다.");
        }

        OAuthProvider providerEnum = client.provider();

        Optional<Member> linked = memberService.findByOAuth(providerEnum, userInfo.getProviderId());
        if (linked.isPresent()) {
            return ResponseEntity.ok(loginAndRespond(session, linked.get()));
        }

        if (userInfo.isEmailVerified()) {
            Optional<Member> byEmail = memberService.findByEmail(userInfo.getEmail());
            if (byEmail.isPresent()) {
                Member member = memberService.linkOAuth(byEmail.get(), providerEnum, userInfo.getProviderId());
                return ResponseEntity.ok(loginAndRespond(session, member));
            }
        }

        session.setAttribute(SESSION_PENDING_PROVIDER, providerEnum.name());
        session.setAttribute(SESSION_PENDING_ID, userInfo.getProviderId());
        session.setAttribute(SESSION_PENDING_EMAIL, userInfo.getEmail());
        String suggestedNickname = userInfo.getEmail().split("@")[0];
        if (suggestedNickname.length() > 12) {
            suggestedNickname = suggestedNickname.substring(0, 12);
        }
        return ResponseEntity.ok(OAuthCallbackResponse.signupRequired(userInfo.getEmail(), suggestedNickname));
    }

    @PostMapping("/signup")
    public ResponseEntity<OAuthCallbackResponse> signup(@Valid @RequestBody OAuthSignupCompleteRequest request,
                                                          HttpSession session) {
        String providerName = (String) session.getAttribute(SESSION_PENDING_PROVIDER);
        String oauthId = (String) session.getAttribute(SESSION_PENDING_ID);
        String email = (String) session.getAttribute(SESSION_PENDING_EMAIL);
        if (providerName == null || oauthId == null || email == null) {
            throw new IllegalArgumentException("진행 중인 SNS 가입 정보가 없습니다. 처음부터 다시 시도해주세요.");
        }

        Member member = memberService.signupViaOAuth(email, OAuthProvider.valueOf(providerName), oauthId,
                request.getNickname(), request.getSupportedParty(), request.getSignupChannel(),
                request.getZipcode(), request.getAddressBase(), request.getAddressDetail(),
                request.isAgreeMarketing());

        session.removeAttribute(SESSION_PENDING_PROVIDER);
        session.removeAttribute(SESSION_PENDING_ID);
        session.removeAttribute(SESSION_PENDING_EMAIL);

        return ResponseEntity.ok(loginAndRespond(session, member));
    }

    private OAuthCallbackResponse loginAndRespond(HttpSession session, Member member) {
        session.setAttribute("loginMemberId", member.getId());
        MemberResponse response = new MemberResponse(member.getId(), member.getLoginId(), member.getEmail(),
                member.getNickname(), member.getRole());
        return OAuthCallbackResponse.login(response);
    }

    private OAuthClient resolveClient(String provider) {
        OAuthClient client = clientsByProvider.get(provider.toLowerCase(Locale.ROOT));
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 SNS 로그인입니다: " + provider);
        }
        return client;
    }
}
