package com.example.team_navigation_server.ad;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

@Service
public class AdService {

    private final AdRepository adRepository;

    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    public Optional<Ad> findActive(String page, String sideValue) {
        AdSide side = parseSide(sideValue);
        LocalDateTime now = LocalDateTime.now();
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
