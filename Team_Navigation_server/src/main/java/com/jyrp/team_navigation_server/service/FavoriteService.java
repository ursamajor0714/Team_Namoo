package com.jyrp.team_navigation_server.service;

import com.jyrp.team_navigation_server.dto.FavoriteDto;
import com.jyrp.team_navigation_server.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public void addFavorite(FavoriteDto dto) {

    }
}
