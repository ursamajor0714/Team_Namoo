package com.jyrp.team_navigation_server.repository;

import com.jyrp.team_navigation_server.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByPlaceId(String placeId);
}
