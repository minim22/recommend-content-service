package com.custom.recommend_content_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.custom.recommend_content_service.properties.TmdbProperties;

@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
public class TmdbConfig {

    @Bean
    public RestClient tmdbRestClient(TmdbProperties properties) {
        
        // 1. 타임아웃설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.timeout().connect().toMillis());
        factory.setReadTimeout((int) properties.timeout().read().toMillis());

        // 2. RestClient 생성
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .defaultHeader("Accept", "application/json")
                .requestFactory(factory)
                .build();
    }
}