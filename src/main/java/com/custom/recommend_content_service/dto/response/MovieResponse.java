package com.custom.recommend_content_service.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.custom.recommend_content_service.entity.Movie;

/** */
public record MovieResponse(
    Long id,
    String title,
    String overview,
    List<Integer> genreIds,
    Double voteAverage,
    Double popularity,
    LocalDate releaseDate,
    String posterPath
) {
    public static MovieResponse from(Movie movies) {
        return new MovieResponse(
            movies.getId(),
            movies.getTitle(),
            movies.getOverview(),
            movies.getGenreIds() != null ? new ArrayList<>(movies.getGenreIds()) : new ArrayList<>(),
            movies.getVoteAverage(),
            movies.getPopularity(),
            movies.getReleaseDate(),
            movies.getPosterPath()
        );
    }

    public static List<MovieResponse> from(List<Movie> movies) {
        return movies.stream()
            .map(MovieResponse::from)
            .toList();
    }
}