package com.example.team_navigation_server.classification;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 정치성향 분류 테스트 파이프라인 전용 - 확인 끝나면 이 패키지 전체를 삭제할 예정.
 */
public interface NewsClassificationRepository extends JpaRepository<NewsClassification, Long> {
}
