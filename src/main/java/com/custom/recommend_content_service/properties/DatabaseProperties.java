package com.custom.recommend_content_service.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record DatabaseProperties(
    String url,
    String username,
    String password,
    String driverClassName,
    Hikari hikari
) {
    public record Hikari(
        int maximumPoolSize,
        int minimumIdle,
        long connectionTimeout
    ) {}
}