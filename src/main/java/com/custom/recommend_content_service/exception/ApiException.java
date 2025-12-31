package com.custom.recommend_content_service.exception;

import com.custom.recommend_content_service.enums.ResultCode;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ResultCode resultCode;
    private final String customMessage;

    /**
     * 기본 응답값
     * @param resultCode
     */
    public ApiException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.customMessage = null;
    }

    /**
     * 커스텀 메시지 생성자 - 동적 메시지 사용
     */
    public ApiException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.resultCode = resultCode;
        this.customMessage = customMessage;
    }
}