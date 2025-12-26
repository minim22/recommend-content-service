package com.custom.recommend_content_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.custom.recommend_content_service.common.ApiResult;
import com.custom.recommend_content_service.dto.response.GameResponse;
import com.custom.recommend_content_service.dto.response.IgdbIngestionResponse;
import com.custom.recommend_content_service.dto.response.SpotifyIngestionResponse;
import com.custom.recommend_content_service.service.MusicIngestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingestion/music")
@Tag(name = "Music", description = "음악 API 조회")
public class MusicIngestionController {
    
    private final MusicIngestionService musicIngestionService;

    @Operation(
        summary = "SPOTIFY 음악 영화 외부 조회",
        description = "SPOTIFY API를 직접 호출하여 현재 인기 음악 목록을 가져옵니다."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "음악 목록 조회 성공"
    )
    @GetMapping("/spotify")
    public ResponseEntity<ApiResult<SpotifyIngestionResponse>> igdbPopularMovies() {

        SpotifyIngestionResponse response =
            musicIngestionService.spotifyPopularMusics();
        
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "수집된 인기 음악 조회",
        description = "시스템 내부에 저장된 인기 음악 목록을 조회합니다"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "메인 음악 목록 조회 성공"
    )
    @GetMapping("/main")
    public ResponseEntity<ApiResult<List<GameResponse>>> getIngestPopularMusics() {

        List<GameResponse> response =
            musicIngestionService.getIngestPopularMusics();
        
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "클릭횟수 업데이트",
        description = "클릭을 통한 내부 알고리즘 처리를 위해 업데이트"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "게임 클릭횟수 업데이트 성공"
    )
    @PatchMapping("/{musicId}/click")
    public ResponseEntity<ApiResult<Void>> incrementClickCount(@PathVariable Long musicId) {

        musicIngestionService.incrementClickCount(musicId);
        
        return ResponseEntity.ok(ApiResult.success());
    }
}