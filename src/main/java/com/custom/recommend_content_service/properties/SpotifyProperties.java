package com.custom.recommend_content_service.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify.url")
public record SpotifyProperties(
    String authUrl,
    String baseUrl
){}
