package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.board.Report;
import com.example.team_navigation_server.board.ReportStatus;
import com.example.team_navigation_server.board.ReportTargetType;

import java.time.Instant;

public class AdminReportResponse {
    private final Long id;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final String targetPreview;
    private final String targetAuthor;
    private final String reason;
    private final ReportStatus status;
    private final String reporterNickname;
    private final Instant createdAt;

    public AdminReportResponse(Report report, String targetPreview, String targetAuthor) {
        this.id = report.getId();
        this.targetType = report.getTargetType();
        this.targetId = report.getTargetId();
        this.targetPreview = targetPreview;
        this.targetAuthor = targetAuthor;
        this.reason = report.getReason();
        this.status = report.getStatus();
        this.reporterNickname = report.getReporterMember().getNickname();
        this.createdAt = report.getCreatedAt();
    }

    public Long getId() { return id; }
    public ReportTargetType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getTargetPreview() { return targetPreview; }
    public String getTargetAuthor() { return targetAuthor; }
    public String getReason() { return reason; }
    public ReportStatus getStatus() { return status; }
    public String getReporterNickname() { return reporterNickname; }
    public Instant getCreatedAt() { return createdAt; }
}
