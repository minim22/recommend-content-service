package com.custom.recommend_content_service.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "igdb.url")
public record IgdbProperties(
    String authUrl,
    String baseUrl,
    String imageUrl
) {}