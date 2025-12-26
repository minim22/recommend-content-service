package com.custom.recommend_content_service.dto.response;

/** SPOTIFY에서 수집된 데이터 처리 결과 요약 응답 DTO */
public record SpotifyIngestionResponse(
    int totalCount
) {}