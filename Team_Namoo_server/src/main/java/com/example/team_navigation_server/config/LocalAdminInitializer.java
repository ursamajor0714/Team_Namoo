package com.example.team_navigation_server.config;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 로컬(docker 프로필) 전용 관리자 계정 부트스트랩.
 * H2 인메모리라 재시작마다 사라져서, 기동 시 없으면 다시 만든다.
 * 이메일 인증은 받은 것으로 간주하고 Member 만 직접 넣는다(login() 은 인증 여부를 안 봄).
 * 운영(EC2, 기본 프로필)에는 로드되지 않는다.
 */
@Component
@Profile("docker")
public class LocalAdminInitializer implements CommandLineRunner {

    private static final String ADMIN_LOGIN_ID = "dbrmsgh11";
    private static final String ADMIN_RAW_PASSWORD = "1234";

    private final MemberRepository memberRepository;

    public LocalAdminInitializer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        if (memberRepository.existsByLoginId(ADMIN_LOGIN_ID)) {
            return;
        }
        String encoded = new BCryptPasswordEncoder().encode(ADMIN_RAW_PASSWORD);
        // email/nickname 은 NOT NULL 이라 빈 문자열로 둔다(추가 정보 없음).
        memberRepository.save(new Member(ADMIN_LOGIN_ID, encoded, "", ""));
    }
}
