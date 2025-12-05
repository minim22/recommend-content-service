package com.custom.recommend_content_service.dto.response;

import java.util.List;

/** 실제 TMDB에서 받아오는 항목값 */
public record TmdbMovieDetailResponse(
    long id,
    String title,
    String overview,
    List<Integer> genreIds,
    double voteAverage,
    double popularity,
    String releaseDate,
    String posterPath
) {
    
}