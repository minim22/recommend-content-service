package com.custom.recommend_content_service.config;

import java.util.TimeZone;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableJpaAuditing
public class TimezoneConfig {
    
    @PostConstruct
    public void init() {
        // JVM 전체의 기본 타임존을 한국으로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}