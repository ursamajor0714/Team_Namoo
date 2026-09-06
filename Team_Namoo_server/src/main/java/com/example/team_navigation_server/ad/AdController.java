package com.example.team_navigation_server.ad;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @GetMapping
    public ResponseEntity<AdPublicResponse> getActiveAd(@RequestParam String page, @RequestParam String side) {
        return adService.findActive(page, side)
                .map(ad -> ResponseEntity.ok(new AdPublicResponse(ad)))
                .orElse(ResponseEntity.noContent().build());
    }
}
