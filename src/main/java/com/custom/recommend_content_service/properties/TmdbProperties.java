package com.custom.recommend_content_service.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tmdb.api")
public record TmdbProperties(
    String baseUrl,
    String accessToken,
    Timeout timeout
) {
    public record Timeout(
        Duration connect,
        Duration read
    ) {}
}