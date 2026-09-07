package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import com.example.team_navigation_server.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    public ReportService(ReportRepository reportRepository, PostRepository postRepository,
                          CommentRepository commentRepository, MemberRepository memberRepository) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void reportPost(Long postId, Long memberId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        if (post.getVisibility() != PostVisibility.NORMAL) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
        save(ReportTargetType.POST, postId, memberId, reason);
    }

    @Transactional
    public void reportComment(Long commentId, Long memberId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (comment.getVisibility() != PostVisibility.NORMAL) {
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        }
        save(ReportTargetType.COMMENT, commentId, memberId, reason);
    }

    private void save(ReportTargetType targetType, Long targetId, Long memberId, String reason) {
        Member reporter = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        if (reportRepository.existsByTargetTypeAndTargetIdAndReporterMember(targetType, targetId, reporter)) {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        }
        reportRepository.save(new Report(targetType, targetId, reporter, reason));
    }
}
