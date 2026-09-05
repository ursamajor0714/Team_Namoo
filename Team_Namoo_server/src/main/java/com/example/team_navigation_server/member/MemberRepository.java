package com.example.team_navigation_server.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    Optional<Member> findByLoginId(String loginId);

    Page<Member> findByLoginIdContainingIgnoreCase(String q, Pageable pageable);
    Page<Member> findByNicknameContainingIgnoreCase(String q, Pageable pageable);
    Page<Member> findByEmailContainingIgnoreCase(String q, Pageable pageable);
    Page<Member> findBySupportedParty_NameContainingIgnoreCase(String q, Pageable pageable);
    Page<Member> findBySignupChannelContainingIgnoreCase(String q, Pageable pageable);
    Page<Member> findByStatus(MemberStatus status, Pageable pageable);
}