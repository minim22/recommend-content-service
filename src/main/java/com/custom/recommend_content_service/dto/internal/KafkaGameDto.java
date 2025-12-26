package com.custom.recommend_content_service.dto.internal;

import java.time.LocalDateTime;

import com.custom.recommend_content_service.dto.response.IgdbGameDetailResponse;

/**
 * Kafaka 전용 DTO
 */
public record KafkaGameDto(
    Long id,
    String name,
    String slug,
    String summary,
    Integer clickCnt,
    LocalDateTime createdAt,
    String screenshotUrl
){
    public static KafkaGameDto from(IgdbGameDetailResponse detailResponse) {
        return new KafkaGameDto(
            detailResponse.id(),
            detailResponse.name(),
            detailResponse.slug(),
            detailResponse.summary(),
            0,
            detailResponse.createdAt(),
            detailResponse.getFirstScreenshotImageId()
        );
    }
}
