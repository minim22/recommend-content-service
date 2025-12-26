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
@Table(name = "music")
public class Music {

    @Id
    private Long id;

    @Column(name = "clickCnt")
    Integer clickCnt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "url")
    private String url;
    
    /**
     * Music 엔티티 생성 정적 팩토리 메서드
     */
    public static Music create(
        Long id,
        Integer clickCnt,
        LocalDateTime createdAt,
        String url
    ) {
        return Music.builder()
            .id(id)
            .clickCnt(clickCnt)
            .createdAt(LocalDateTime.now())
            .url(url)
            .build();
    }

    /**
     * Music 엔티티 업데이트 메서드
     */
    public void update(
        String url
    ) {
        this.url = url;
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