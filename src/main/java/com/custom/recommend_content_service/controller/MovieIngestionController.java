package com.custom.recommend_content_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.custom.recommend_content_service.common.ApiResult;
import com.custom.recommend_content_service.dto.response.IngestionResponse;
import com.custom.recommend_content_service.enums.SuccessCode;
import com.custom.recommend_content_service.service.MovieIngestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingestion")
@Tag(name = "Movie", description = "영화 API 조회")
public class MovieIngestionController {
    
    private final MovieIngestionService movieIngestionService;

    @Operation(summary = "영화 목록 조회", description = "전체 영화 목록을 반환합니다")
    @ApiResponse(responseCode = "200", description = "등기권리증 정보 조회 성공")
    @GetMapping("/movie/popular")
    public ResponseEntity<ApiResult<IngestionResponse>> ingestPopularMovies(@RequestParam String param) {

        IngestionResponse response = movieIngestionService.ingestPopularMovies();
        
        return ResponseEntity.ok(ApiResult.success(response));
    }
    
}