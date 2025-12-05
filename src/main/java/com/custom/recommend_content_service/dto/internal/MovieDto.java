package com.custom.recommend_content_service.dto.internal;

import java.util.List;

import com.custom.recommend_content_service.dto.response.TmdbMovieDetailResponse;

public record MovieDto(
    long id,
    String title,
    String overview,
    List<Integer> genreIds,
    double voteAverage,
    double popularity,
    String releaseDate,
    String posterPath
) {
    public static MovieDto from(TmdbMovieDetailResponse detailResponse) {
        return new MovieDto(
            detailResponse.id(),
            detailResponse.title(),
            detailResponse.overview(),
            detailResponse.genreIds(),
            detailResponse.voteAverage(),
            detailResponse.popularity(),
            detailResponse.releaseDate(),
            detailResponse.posterPath()
        );
    }
}