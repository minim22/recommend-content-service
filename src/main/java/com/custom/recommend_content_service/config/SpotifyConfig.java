package com.custom.recommend_content_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.custom.recommend_content_service.properties.SpotifyProperties;

@Configuration
@EnableConfigurationProperties(SpotifyProperties.class)
public class SpotifyConfig {
    
    /**
     * Spotify OAuth2 인증용 RestClient
     */
    @Bean
    public RestClient spotifyAuthRestClient(SpotifyProperties properties) {
        return RestClient.builder()
            .baseUrl(properties.authUrl())
            .build();
    }

    /**
     * Spotify API용 RestClient
     */
    @Bean
    public RestClient spotifyApiRestClient(SpotifyProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
