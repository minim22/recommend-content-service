package com.custom.recommend_content_service.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 실제 TMDB에서 받아오는 항목값 */
public record TmdbMovieDetailResponse(

    @JsonProperty("id")
    Long id,

    @JsonProperty("title")
    String title,

    @JsonProperty("overview")
    String overview,

    @JsonProperty("genre_ids")
    List<Integer> genreIds,

    @JsonProperty("vote_average")
    Double voteAverage,

    @JsonProperty("popularity")
    Double popularity,

    @JsonProperty("release_date")
    String releaseDate,

    @JsonProperty("poster_path")
    String posterPath
) {}