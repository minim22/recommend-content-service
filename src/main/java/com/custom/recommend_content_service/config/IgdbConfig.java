package com.custom.recommend_content_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.custom.recommend_content_service.properties.IgdbProperties;

@Configuration
@EnableConfigurationProperties(IgdbProperties.class)
public class IgdbConfig {
    
    /**
     * Twitch OAuth2 인증용 RestClient
     */
    @Bean
    public RestClient twitchAuthRestClient(IgdbProperties properties) {
        return RestClient.builder()
            .baseUrl(properties.authUrl())
            .build();
    }

    /**
     * IGDB API용 RestClient
     */
    @Bean
    public RestClient igdbRestClient(IgdbProperties properties) {

        // 1. 타임아웃 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 2. RestClient 생성
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Accept", "application/json")
                .requestFactory(factory)
                .build();
    }
}