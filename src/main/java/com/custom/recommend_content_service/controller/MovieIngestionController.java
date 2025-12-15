package com.custom.recommend_content_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.custom.recommend_content_service.common.ApiResult;
import com.custom.recommend_content_service.dto.response.MovieResponse;
import com.custom.recommend_content_service.dto.response.TmdbIngestionResponse;
import com.custom.recommend_content_service.service.MovieIngestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingestion/movie")
@Tag(name = "Movie", description = "영화 API 조회")
public class MovieIngestionController {
    
    private final MovieIngestionService movieIngestionService;

    @Operation(summary = "TMDB 메인 영화 조회", description = "메인에 노출할 영화 목록을 반환합니다")
    @ApiResponse(
        responseCode = "200", 
        description = "영화 목록 조회 성공"
    )
    @GetMapping("/tmdb")
    public ResponseEntity<ApiResult<TmdbIngestionResponse>> tmdbPopularMovies() {

        TmdbIngestionResponse response = movieIngestionService.tmdbPopularMovies();
        
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "메인화면 조회", description = "메인에 노출할 영화 목록을 조회합니다")
    @ApiResponse(
        responseCode = "200", 
        description = "메인 영화 목록 조회 성공"
    )
    @GetMapping("/main")
    public ResponseEntity<ApiResult<List<MovieResponse>>> createIngestPopularMovies() {

        List<MovieResponse> response = movieIngestionService.getIngestPopularMovies();
        
        return ResponseEntity.ok(ApiResult.success(response));
    }
}