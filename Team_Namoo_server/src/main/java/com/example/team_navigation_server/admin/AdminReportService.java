package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.board.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public AdminReportService(ReportRepository reportRepository, PostRepository postRepository,
                               CommentRepository commentRepository) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public Page<AdminReportResponse> search(String status, String targetType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        ReportStatus statusValue = (status == null || status.isBlank()) ? null : parseStatus(status);
        ReportTargetType targetTypeValue = (targetType == null || targetType.isBlank()) ? null : parseTargetType(targetType);

        Page<Report> reports;
        if (statusValue != null && targetTypeValue != null) {
            reports = reportRepository.findByStatusAndTargetType(statusValue, targetTypeValue, pageable);
        } else if (statusValue != null) {
            reports = reportRepository.findByStatus(statusValue, pageable);
        } else if (targetTypeValue != null) {
            reports = reportRepository.findByTargetType(targetTypeValue, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        return reports.map(this::toResponse);
    }

    public void updateStatus(Long id, String statusValue) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));
        report.setStatus(parseStatus(statusValue));
        reportRepository.save(report);
    }

    private AdminReportResponse toResponse(Report report) {
        String preview;
        String author;
        if (report.getTargetType() == ReportTargetType.POST) {
            Post post = postRepository.findById(report.getTargetId()).orElse(null);
            preview = post != null ? post.getTitle() : "(삭제된 게시글)";
            author = post != null ? post.getAuthorName() : null;
        } else {
            Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
            preview = comment != null ? comment.getContent() : "(삭제된 댓글)";
            author = comment != null ? comment.getAuthorName() : null;
        }
        return new AdminReportResponse(report, preview, author);
    }

    private ReportStatus parseStatus(String value) {
        try {
            return ReportStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("처리 상태는 PENDING/RESOLVED/REJECTED 중 하나여야 합니다.");
        }
    }

    private ReportTargetType parseTargetType(String value) {
        try {
            return ReportTargetType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("대상 유형은 POST/COMMENT 중 하나여야 합니다.");
        }
    }
}
