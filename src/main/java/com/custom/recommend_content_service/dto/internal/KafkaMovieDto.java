package com.custom.recommend_content_service.dto.internal;

import java.util.List;

import com.custom.recommend_content_service.dto.response.TmdbMovieDetailResponse;

/**
 * Kafaka 전용 DTO
 */
public record KafkaMovieDto(
    Long id,
    String title,
    String overview,
    List<Integer> genreIds,
    Double voteAverage,
    Double popularity,
    String releaseDate,
    String posterPath
){
    public static KafkaMovieDto from(TmdbMovieDetailResponse detailResponse) {
        return new KafkaMovieDto(
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
