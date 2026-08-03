package com.jyrp.team_navigation_server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchResponse {
    private String placeId;
    private String name;
    private Double lat;
    private Double lng;
    private Double rating;
    private String category;
    private String photoUrl;
    private Boolean openNow;

}
