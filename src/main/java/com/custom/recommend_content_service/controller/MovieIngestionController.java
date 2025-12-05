package com.custom.recommend_content_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.custom.recommend_content_service.common.ApiResponse;
import com.custom.recommend_content_service.dto.response.IngestionResponse;
import com.custom.recommend_content_service.service.MovieIngestionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingestion")
public class MovieIngestionController {
    
    private final MovieIngestionService movieIngestionService;

    @GetMapping("/movie/popular")
    public ResponseEntity<ApiResponse<IngestionResponse>> ingestPopularMovies(@RequestParam String param) {

        IngestionResponse response = movieIngestionService.ingestPopularMovies();
        
        return ApiResponse.success(response).toEntity();
    }
    
}