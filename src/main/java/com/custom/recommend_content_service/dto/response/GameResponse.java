package com.custom.recommend_content_service.dto.response;

import java.time.LocalDateTime;

import com.custom.recommend_content_service.entity.Game;

public record GameResponse(
    Long id,
    String name,
    String summary,
    LocalDateTime createdAt,
    String screenshotUrl
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
            game.getId(),
            game.getName(),
            game.getSummary(),
            game.getCreatedAt(),
            game.getScreenshotUrl()
        );
    }
}