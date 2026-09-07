package com.example.team_navigation_server.member;

import com.example.team_navigation_server.email.EmailVerificationService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final EmailVerificationService emailVerificationService;
    private final PartyRepository partyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public MemberService(MemberRepository memberRepository, EmailVerificationService emailVerificationService,
                          PartyRepository partyRepository) {
        this.memberRepository = memberRepository;
        this.emailVerificationService = emailVerificationService;
        this.partyRepository = partyRepository;
    }

    // 회원가입
    public void signup(SignupRequest request){
        if (!emailVerificationService.isVerified(request.getEmail())) {
            throw new IllegalArgumentException("이메일 인증을 완료해주세요.");
        }
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        validateNickname(request.getNickname());
        Party party = resolveParty(request.getSupportedParty());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member member = new Member(request.getLoginId(), encodedPassword, request.getEmail(), request.getNickname());
        member.setEmailVerified(true);
        member.setSupportedParty(party);
        member.setSignupChannel(request.getSignupChannel());
        member.setZipcode(request.getZipcode());
        member.setAddressBase(request.getAddressBase());
        member.setAddressDetail(request.getAddressDetail());
        member.setAgreeMarketing(request.isAgreeMarketing());
        memberRepository.save(member);
    }
    // 로그인
    public Member login(LoginRequest request){
        Member member = memberRepository.findByLoginId(request.getLoginId()).
                orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
                if(!passwordEncoder.matches(request.getPassword(), member.getPassword())){
                    throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
                return assertActiveAndTouch(member);
    }

    // SNS 로그인 - 이미 연결된 계정 조회 (OAuthLoginController 에서 사용)
    public Optional<Member> findByOAuth(OAuthProvider provider, String oauthId) {
        return memberRepository.findByOauthProviderAndOauthId(provider, oauthId);
    }

    // SNS 이메일이 검증된 상태일 때만 호출 - 같은 이메일의 기존 일반 가입 계정을 찾아 연결 대상으로 삼는다.
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // 기존 일반 가입 계정에 SNS 로그인을 최초로 연결할 때
    public Member linkOAuth(Member member, OAuthProvider provider, String oauthId) {
        member.setOauthProvider(provider);
        member.setOauthId(oauthId);
        return assertActiveAndTouch(member);
    }

    // SNS 최초 로그인 - 연결된 계정도, 이메일이 겹치는 기존 계정도 없을 때 프로필을 마저 입력받아 신규 가입
    public Member signupViaOAuth(String email, OAuthProvider provider, String oauthId,
                                  String nickname, String supportedParty, String signupChannel,
                                  String zipcode, String addressBase, String addressDetail,
                                  boolean agreeMarketing) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        validateNickname(nickname);
        Party party = resolveParty(supportedParty);

        // SNS 전용 계정 - loginId/password로는 로그인할 수 없게 임의값(비밀번호는 무작위 해시)으로 채운다.
        String syntheticLoginId = provider.name().toLowerCase() + "_" + oauthId;
        String unusablePassword = passwordEncoder.encode(UUID.randomUUID().toString());
        Member member = new Member(syntheticLoginId, unusablePassword, email, nickname);
        member.setEmailVerified(true);
        member.setSupportedParty(party);
        member.setSignupChannel(signupChannel);
        member.setZipcode(zipcode);
        member.setAddressBase(addressBase);
        member.setAddressDetail(addressDetail);
        member.setAgreeMarketing(agreeMarketing);
        member.setOauthProvider(provider);
        member.setOauthId(oauthId);
        return memberRepository.save(member);
    }

    private Member assertActiveAndTouch(Member member) {
        assertActive(member);
        member.setLastAccessAt(LocalDateTime.now());
        return memberRepository.save(member);
    }

    private void assertActive(Member member) {
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new IllegalArgumentException("정지된 계정입니다.");
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }
    }

    // 로그인 시점 이후(세션이 이미 있는 상태)에서도 글쓰기/댓글/추천/신고 등 매 쓰기 액션마다
    // 이걸로 재확인해야 한다 - 정지는 로그인 때 한 번만 걸리면 이미 로그인된 세션은 못 막는다.
    public Member requireActiveMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        assertActive(member);
        return member;
    }

    private void validateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        String lowerNickname = nickname.toLowerCase();
        for (String word : bannedWords) {
            if (lowerNickname.contains(word.toLowerCase())) {
                throw new IllegalArgumentException("사용할 수 없는 닉네임입니다.");
            }
        }
    }

    private Party resolveParty(String name) {
        return partyRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정당입니다."));
    }

    public MemberResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return new MemberResponse(member.getId(), member.getLoginId(), member.getEmail(), member.getNickname(), member.getRole());
    }

    public boolean isLoginIdAvailable(String loginId) {
        return !memberRepository.existsByLoginId(loginId);
    }

    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }
    private final Set<String> bannedWords = Set.of(
            "시발", "씨발", "씨팔", "개새끼","새끼", "병신", "지랄", "좆", "자지","보지","엠창","느금",
        "니애미","창녀","한남","한녀","김치녀","된장녀","틀딱","급식충","맘충","전라디언",
            "홍어","일베","메갈","워마드","fuck","shit","bitch","asshole","nigger");


}
