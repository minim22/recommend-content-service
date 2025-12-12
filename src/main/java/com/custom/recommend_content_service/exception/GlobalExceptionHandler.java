package com.custom.recommend_content_service.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.custom.recommend_content_service.common.ApiResult;

/**
 * 전역 예외 처리 핸들러
 * 모든 Controller에서 발생하는 예외를 일관된 형식으로 처리
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResult<Object>> handleBusinessException(ApiException e) {
        
        log.warn("BusinessException: code={}, message={}",
                e.getResultCode().getCode(), e.getResultCode().getMessage());

        return ApiResult.error(e.getResultCode()).toEntity();
    }
}
