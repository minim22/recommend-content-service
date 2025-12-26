package com.custom.recommend_content_service.entity;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class) 
@Table(name = "game")
public class Game {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "clickCnt")
    Integer clickCnt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "screenshot_url")
    private String screenshotUrl;
    
    /**
     * Game 엔티티 생성 정적 팩토리 메서드
     */
    public static Game create(
        Long id,
        String name,
        String slug,
        String summary,
        Integer clickCnt,
        LocalDateTime createdAt,
        String screenshotUrl) {
        return Game.builder()
            .id(id)
            .name(name)
            .slug(slug)
            .summary(summary)
            .clickCnt(clickCnt)
            .createdAt(LocalDateTime.now())
            .screenshotUrl(screenshotUrl)
            .build();
    }

    /**
     * Game 엔티티 업데이트 메서드
     */
    public void update(
        String name,
        String slug,
        String summary,
        String screenshotUrl
    ) {
        this.name = name;
        this.slug = slug;
        this.summary = summary;
        this.screenshotUrl = screenshotUrl;
    }

    /**
     * 클릭 수 증가
     */
    public void incrementClickCount() {
        if (this.clickCnt == null) {
            this.clickCnt = 0;
        }
        this.clickCnt++;
    }
}