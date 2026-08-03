package com.jyrp.team_navigation_server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name="favorites")
@EntityListeners(AuditingEntityListener.class)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //(PK, 자동증가)
    private String placeId; //(google place id)
    private String name;
    private String address;
    private String category;
    private Double lat;
    private Double lng;
    private String imageUrl;
    private Double rating;
    @CreatedDate
    private LocalDateTime createdAt;

    public Favorite(String placeId, String name, String address, String category, Double lat,
                    Double lng, String imageUrl, Double rating){
        this.placeId = placeId;
        this.name = name;
        this.address = address;
        this.category = category;
        this.lat = lat;
        this.lng = lng;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }
}
