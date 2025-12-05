package com.custom.recommend_content_service.common;

import java.time.LocalDateTime;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.custom.recommend_content_service.enums.ResultCode;
import com.custom.recommend_content_service.enums.SuccessCode;


public record ApiResponse<T>(
    int status,
    String code,
    String title,
    String message,
    T data,
    LocalDateTime timestamp,
    String transactionId
){
    // 1. 기본 성공 응답 (200 OK) -> 하드코딩 대신 Enum 사용
    public static <T> ApiResponse<T> success(T data) {
        return of(SuccessCode.OK, data);
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    // 2. 실패 응답
    public static <T> ApiResponse<T> error(ResultCode resultCode) {
        return of(resultCode, null);
    }

    // 3. (공통) 내부 생성 로직 - 성공/실패 모두 여기서 처리
    private static <T> ApiResponse<T> of(ResultCode resultCode, T data) {
        return new ApiResponse<>(
            resultCode.getStatus(),
            resultCode.getCode(),
            resultCode.getTitle(),
            resultCode.getMessage(),
            data,
            LocalDateTime.now(),
            UUID.randomUUID().toString()
        );
    }

    public ResponseEntity<ApiResponse<T>> toEntity() {
        return ResponseEntity
                .status(this.status)
                .body(this);
    }
}