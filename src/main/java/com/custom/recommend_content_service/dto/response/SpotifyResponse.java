package com.custom.recommend_content_service.dto.response;

import java.time.LocalDateTime;

import com.custom.recommend_content_service.entity.Music;

public record SpotifyResponse(
    Long id,
    LocalDateTime createdAt,
    String url
) {
    public static SpotifyResponse from(Music music) {
        return new SpotifyResponse(
            music.getId(),
            music.getCreatedAt(),
            music.getUrl()
        );
    }
}