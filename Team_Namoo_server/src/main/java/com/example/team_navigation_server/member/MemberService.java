package com.example.team_navigation_server.member;

import com.example.team_navigation_server.email.EmailVerificationService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

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
        for (String word : bannedWords) {
            if (request.getNickname().contains(word)) {
                throw new IllegalArgumentException("사용할 수 없는 닉네임입니다.");
            }
        }
        Party party = partyRepository.findByName(request.getSupportedParty())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정당입니다."));

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
                if (member.getStatus() == MemberStatus.SUSPENDED) {
                    throw new IllegalArgumentException("정지된 계정입니다.");
                }
                if (member.getStatus() == MemberStatus.WITHDRAWN) {
                    throw new IllegalArgumentException("탈퇴한 계정입니다.");
                }
                member.setLastAccessAt(LocalDateTime.now());
                memberRepository.save(member);
                return member;
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
    private final Set<String> bannedWords = Set.of(
            "시발", "씨발", "씨팔", "개새끼","새끼", "병신", "지랄", "좆", "자지","보지","엠창","느금",
        "니애미","창녀","한남","한녀","김치녀","된장녀","틀딱","급식충","맘충","전라디언",
            "홍어","일베","메갈","워마드","fuck","shit","bitch","asshole","nigger");


}
