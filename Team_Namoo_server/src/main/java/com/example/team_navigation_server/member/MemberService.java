package com.example.team_navigation_server.member;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원가입
    public void signup(SignupRequest request){
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member member = new Member(request.getLoginId(), encodedPassword, request.getEmail(), request.getNickname());
        memberRepository.save(member);
    }
    // 로그인
    public Member login(LoginRequest request){
        Member member = memberRepository.findByLoginId(request.getLoginId()).
                orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
                if(!passwordEncoder.matches(request.getPassword(), member.getPassword())){
                    throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
                return member;
    }
    public MemberResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return new MemberResponse(member.getId(), member.getLoginId(), member.getEmail(), member.getNickname());
    }
}
