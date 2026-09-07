package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import jakarta.persistence.*;

import java.time.Instant;

// 게시글/댓글 신고 1건. 같은 대상은 회원 1명당 1번만 신고 가능(유니크 제약).
@Entity
@Table(name = "reports", uniqueConstraints = @UniqueConstraint(columnNames = {"target_type", "target_id", "reporter_member_id"}))
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_member_id", nullable = false)
    private Member reporterMember;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Report() {
    }

    public Report(ReportTargetType targetType, Long targetId, Member reporterMember, String reason) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.reporterMember = reporterMember;
        this.reason = reason;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public Member getReporterMember() {
        return reporterMember;
    }

    public String getReason() {
        return reason;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
