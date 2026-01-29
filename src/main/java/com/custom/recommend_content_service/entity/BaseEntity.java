package com.custom.recommend_content_service.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 공통처리하는 entity
 */
@Getter
@MappedSuperclass // 이 클래스를 상속받는 엔티티들이 아래 필드들을 컬럼으로 인식하게 함
@EntityListeners(AuditingEntityListener.class) // 자동으로 시간을 주입하는 리스너
public abstract class BaseEntity {

    @CreatedDate // 생성 시 자동으로 시간 저장
    @Column(updatable = false) // 수정 시에는 건드리지 않음
    private LocalDateTime createdAt;

    @LastModifiedDate // 수정 시 자동으로 시간 갱신
    private LocalDateTime updatedAt;
}