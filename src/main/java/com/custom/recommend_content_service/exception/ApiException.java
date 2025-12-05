package com.custom.recommend_content_service.exception;

import com.custom.recommend_content_service.enums.ResultCode;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ResultCode resultCode;

    public ApiException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
}