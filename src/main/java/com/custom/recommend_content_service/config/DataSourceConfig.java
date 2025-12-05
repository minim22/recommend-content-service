package com.custom.recommend_content_service.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.custom.recommend_content_service.properties.DatabaseProperties;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(DatabaseProperties properties) {
        // Spring Boot의 기본 커넥션 풀인 HikariCP를 명시적으로 생성
        HikariDataSource dataSource = new HikariDataSource();

        // 1. 기본 연결 정보 주입
        dataSource.setDriverClassName(properties.driverClassName());
        dataSource.setJdbcUrl(properties.url());
        dataSource.setUsername(properties.username());
        dataSource.setPassword(properties.password());

        // 2. HikariCP 세부 튜닝
        if (properties.hikari() != null) {
            dataSource.setMaximumPoolSize(properties.hikari().maximumPoolSize());
            dataSource.setMinimumIdle(properties.hikari().minimumIdle());
            dataSource.setConnectionTimeout(properties.hikari().connectionTimeout());
        }

        return dataSource;
    }
}
