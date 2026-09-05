package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.member.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminMemberService {

    private static final int INACTIVE_DAYS = 30;

    private final MemberRepository memberRepository;
    private final PartyRepository partyRepository;

    public AdminMemberService(MemberRepository memberRepository, PartyRepository partyRepository) {
        this.memberRepository = memberRepository;
        this.partyRepository = partyRepository;
    }

    public Page<AdminMemberResponse> search(String field, String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Member> members;

        if (field == null || field.isBlank() || q == null || q.isBlank()) {
            members = memberRepository.findAll(pageable);
        } else {
            members = switch (field) {
                case "loginId" -> memberRepository.findByLoginIdContainingIgnoreCase(q, pageable);
                case "nickname" -> memberRepository.findByNicknameContainingIgnoreCase(q, pageable);
                case "email" -> memberRepository.findByEmailContainingIgnoreCase(q, pageable);
                case "supportedParty" -> memberRepository.findBySupportedParty_NameContainingIgnoreCase(q, pageable);
                case "signupChannel" -> memberRepository.findBySignupChannelContainingIgnoreCase(q, pageable);
                case "status" -> memberRepository.findByStatus(parseStatus(q), pageable);
                default -> throw new IllegalArgumentException("지원하지 않는 검색 조건입니다.");
            };
        }
        return members.map(AdminMemberResponse::new);
    }

    public AdminMemberResponse getDetail(Long id) {
        return new AdminMemberResponse(findMember(id));
    }

    public void update(Long id, AdminMemberUpdateRequest request) {
        Member member = findMember(id);

        if (request.getNickname() != null) member.setNickname(request.getNickname());
        if (request.getEmail() != null) member.setEmail(request.getEmail());
        if (request.getEmailVerified() != null) member.setEmailVerified(request.getEmailVerified());
        if (request.getSignupChannel() != null) member.setSignupChannel(request.getSignupChannel());
        if (request.getZipcode() != null) member.setZipcode(request.getZipcode());
        if (request.getAddressBase() != null) member.setAddressBase(request.getAddressBase());
        if (request.getAddressDetail() != null) member.setAddressDetail(request.getAddressDetail());
        if (request.getIsPlus() != null) member.setPlus(request.getIsPlus());
        if (request.getAgreeMarketing() != null) member.setAgreeMarketing(request.getAgreeMarketing());
        if (request.getSupportedParty() != null) {
            Party party = partyRepository.findByName(request.getSupportedParty())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정당입니다."));
            member.setSupportedParty(party);
        }

        memberRepository.save(member);
    }

    public void updateStatus(Long id, MemberStatus status) {
        Member member = findMember(id);
        if (member.getRole() == MemberRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("슈퍼관리자는 정지할 수 없습니다.");
        }
        member.setStatus(status);
        memberRepository.save(member);
    }

    public void updateRole(Long id, MemberRole role, Member actingAdmin) {
        if (actingAdmin.getRole() != MemberRole.SUPER_ADMIN) {
            throw new AdminForbiddenException("관리자 임명/해제는 슈퍼관리자만 할 수 있습니다.");
        }
        if (role == MemberRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("이 API로는 슈퍼관리자를 임명할 수 없습니다.");
        }
        Member member = findMember(id);
        if (member.getRole() == MemberRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("슈퍼관리자의 권한은 변경할 수 없습니다.");
        }
        member.setRole(role);
        memberRepository.save(member);
    }

    public Map<String, Object> stats() {
        var all = memberRepository.findAll();
        long total = all.size();
        long active = all.stream().filter(m -> m.getStatus() == MemberStatus.ACTIVE).count();
        long plus = all.stream().filter(Member::isPlus).count();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(INACTIVE_DAYS);
        long inactive = all.stream()
                .filter(m -> m.getLastAccessAt() == null || m.getLastAccessAt().isBefore(cutoff))
                .count();

        double denom = total == 0 ? 1 : total;
        return Map.of(
                "activeCount", active,
                "inactiveCount", inactive,
                "inactivePct", Math.round(inactive / denom * 1000) / 10.0,
                "plusCount", plus,
                "plusPct", Math.round(plus / denom * 1000) / 10.0,
                "weeklyRevenue", 0
        );
    }

    private MemberStatus parseStatus(String q) {
        try {
            return MemberStatus.valueOf(q.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("상태 값은 ACTIVE/SUSPENDED/WITHDRAWN 이어야 합니다.");
        }
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }
}
