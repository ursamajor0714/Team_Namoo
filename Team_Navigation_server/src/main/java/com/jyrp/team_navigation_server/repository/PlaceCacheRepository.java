package com.jyrp.team_navigation_server.repository;

import com.jyrp.team_navigation_server.entity.PlaceCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceCacheRepository extends JpaRepository<PlaceCache, Long> {
    Optional<PlaceCache> findByPlaceId(String placeId);
}
