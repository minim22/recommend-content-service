package com.custom.recommend_content_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.custom.recommend_content_service",
    "com.custom.recommend_kafka"
})
@ConfigurationPropertiesScan(basePackages = {
    "com.custom.recommend_content_service",
    "com.custom.recommend_kafka"
})
public class RecommendContentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecommendContentServiceApplication.class, args);
	}

}
