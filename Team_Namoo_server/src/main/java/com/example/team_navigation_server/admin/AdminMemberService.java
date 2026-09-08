package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.ad.AdRepository;
import com.example.team_navigation_server.board.CommentRepository;
import com.example.team_navigation_server.board.PostRepository;
import com.example.team_navigation_server.board.PostVoteRepository;
import com.example.team_navigation_server.board.PostVoteType;
import com.example.team_navigation_server.board.ReportRepository;
import com.example.team_navigation_server.member.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminMemberService {

    private static final int INACTIVE_DAYS = 30;

    /** 탈퇴 회원이 쓴 글/댓글에 남길 표시명. 글 자체는 지우지 않고 작성자 연결만 끊는다. */
    private static final String WITHDRAWN_AUTHOR_NAME = "탈퇴한 회원";

    private final MemberRepository memberRepository;
    private final PartyRepository partyRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostVoteRepository postVoteRepository;
    private final ReportRepository reportRepository;
    private final AdRepository adRepository;

    public AdminMemberService(MemberRepository memberRepository,
                              PartyRepository partyRepository,
                              PostRepository postRepository,
                              CommentRepository commentRepository,
                              PostVoteRepository postVoteRepository,
                              ReportRepository reportRepository,
                              AdRepository adRepository) {
        this.memberRepository = memberRepository;
        this.partyRepository = partyRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postVoteRepository = postVoteRepository;
        this.reportRepository = reportRepository;
        this.adRepository = adRepository;
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

    /**
     * 관리자에 의한 회원 탈퇴 - 회원 행을 DB 에서 실제로 지운다(복구 불가).
     *
     * 회원을 참조하는 데이터가 있어 그대로 지우면 외래키 제약에 걸리므로 먼저 정리한다.
     *  - 추천/비추천 기록: 삭제하고, 그만큼 글의 추천수를 되돌린다
     *  - 그 회원이 넣은 신고: 삭제 (신고자 컬럼이 NOT NULL 이라 남길 수 없다)
     *  - 그 회원이 등록한 광고: 광고는 남기고 등록자 연결만 끊는다
     *  - 글/댓글: 글타래가 끊기지 않게 남기고, 작성자 연결만 끊은 뒤 표시명을 '탈퇴한 회원' 으로 바꾼다
     */
    @Transactional
    public void delete(Long id, Member actingAdmin) {
        Member member = findMember(id);

        if (actingAdmin != null && actingAdmin.getId().equals(member.getId())) {
            throw new IllegalArgumentException("자기 자신은 탈퇴시킬 수 없습니다.");
        }
        if (member.getRole() != MemberRole.USER) {
            throw new IllegalArgumentException("관리자 계정은 탈퇴시킬 수 없습니다. 먼저 권한을 회원으로 되돌려주세요.");
        }

        postVoteRepository.findByMember(member).forEach(vote -> {
            if (vote.getType() == PostVoteType.LIKE) {
                vote.getPost().decreaseLikes();
            } else {
                vote.getPost().decreaseDislikes();
            }
            postVoteRepository.delete(vote);
        });

        reportRepository.deleteAll(reportRepository.findByReporterMember(member));
        adRepository.findByCreatedBy(member).forEach(ad -> ad.detachCreatedBy());
        postRepository.findByAuthorMember(member).forEach(post -> post.detachAuthor(WITHDRAWN_AUTHOR_NAME));
        commentRepository.findByAuthorMember(member).forEach(comment -> comment.detachAuthor(WITHDRAWN_AUTHOR_NAME));

        memberRepository.delete(member);
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
