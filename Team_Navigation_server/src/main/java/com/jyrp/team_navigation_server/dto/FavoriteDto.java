package com.jyrp.team_navigation_server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDto {
    private Long id;
    private String placeId;
    private String name;
    private String address;
    private String category;
    private Double lat;
    private Double lng;
    private String imageUrl;
    private Double rating;
    private LocalDateTime createdAt;
}
