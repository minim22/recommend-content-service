package com.custom.recommend_content_service.dto.response;

import java.util.List;

/** 실제 TMDB에서 받아오는 항목값 */
public record TmdbMovieResponse(
    int page,
    List<TmdbMovieDetailResponse> results
) {
    
}