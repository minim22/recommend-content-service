package com.custom.recommend_content_service.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // /api/** 경로에 대해
                .allowedOrigins("http://localhost:5173")  // Vue 개발 서버
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // preflight 캐싱 시간
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 절대 경로인지 확인
        String location;
        if (Paths.get(uploadPath).isAbsolute()) {
            location = "file:" + uploadPath + "/";
        } else {
            // 상대 경로면 프로젝트 루트 기준
            location = "file:" + System.getProperty("user.dir") + "/" + uploadPath + "/";
        }
        
        registry.addResourceHandler("/images/games/**")
                .addResourceLocations(location);
    }

}
