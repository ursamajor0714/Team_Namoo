package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByTargetTypeAndTargetIdAndReporterMember(ReportTargetType targetType, Long targetId, Member reporterMember);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    Page<Report> findByTargetType(ReportTargetType targetType, Pageable pageable);

    Page<Report> findByStatusAndTargetType(ReportStatus status, ReportTargetType targetType, Pageable pageable);
}
