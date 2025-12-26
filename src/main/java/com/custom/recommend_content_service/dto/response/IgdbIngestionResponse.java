package com.custom.recommend_content_service.dto.response;

/** IGDB에서 수집된 데이터 처리 결과 요약 응답 DTO */
public record IgdbIngestionResponse(
    int totalCount
) {}