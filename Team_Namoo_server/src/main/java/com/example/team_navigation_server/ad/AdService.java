package com.example.team_navigation_server.ad;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Optional;

@Service
public class AdService {

    // 관리자가 입력하는 startAt/endAt은 시간대 정보 없는(naive) 값이지만 실제로는 KST 기준이다.
    // 서버(EC2) JVM 기본 시간대가 UTC면 LocalDateTime.now()가 9시간 어긋나서 "지금부터 노출"로
    // 등록해도 계속 "예약"으로 보이는 버그가 생긴다 - 항상 Asia/Seoul로 고정해서 비교한다.
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AdRepository adRepository;

    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    public Optional<Ad> findActive(String page, String sideValue) {
        AdSide side = parseSide(sideValue);
        LocalDateTime now = LocalDateTime.now(KST);
        return adRepository.findByPageAndSide(page, side).stream()
                .filter(ad -> (ad.getStartAt() == null || !ad.getStartAt().isAfter(now))
                        && (ad.getEndAt() == null || !ad.getEndAt().isBefore(now)))
                .min(Comparator.comparing(ad -> ad.getStartAt() == null ? LocalDateTime.MIN : ad.getStartAt()));
    }

    private AdSide parseSide(String value) {
        try {
            return AdSide.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("슬롯 위치는 LEFT/RIGHT 중 하나여야 합니다.");
        }
    }
}
