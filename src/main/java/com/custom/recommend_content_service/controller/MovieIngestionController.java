package com.custom.recommend_content_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "TMDB 인기 영화 외부 조회", description = "TMDB API를 직접 호출하여 현재 인기 영화 목록을 가져옵니다.")
    @ApiResponse(
        responseCode = "200", 
        description = "영화 목록 조회 성공"
    )
    @GetMapping("/tmdb")
    public ResponseEntity<ApiResult<TmdbIngestionResponse>> tmdbPopularMovies() {

        TmdbIngestionResponse response = movieIngestionService.tmdbPopularMovies();
        
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "수집된 인기 영화 조회", description = "시스템 내부에 저장된 인기 영화 목록을 조회합니다")
    @ApiResponse(
        responseCode = "200", 
        description = "메인 영화 목록 조회 성공"
    )
    @GetMapping("/main")
    public ResponseEntity<ApiResult<List<MovieResponse>>> createIngestPopularMovies() {

        List<MovieResponse> response = movieIngestionService.getIngestPopularMovies();
        
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "클릭횟수 업데이트",
        description = "클릭을 통한 내부 알고리즘 처리를 위해 업데이트"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "영화 클릭횟수 업데이트 성공"
    )
    @PatchMapping("/{moiveId}/click")
    public ResponseEntity<ApiResult<Void>> incrementClickCount(@PathVariable Long moiveId) {

        movieIngestionService.incrementClickCount(moiveId);
        
        return ResponseEntity.ok(ApiResult.success());
    }
}