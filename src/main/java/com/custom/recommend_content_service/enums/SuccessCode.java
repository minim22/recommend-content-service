package com.custom.recommend_content_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements ResultCode {

    OK(200, "S000", "요청 성공", "정상적으로 처리되었습니다."),
    
    CREATED(201, "S001", "등록 완료", "성공적으로 등록되었습니다."),
    
    ACCEPTED(202, "S002", "접수 완료", "요청이 성공적으로 접수되어 처리 중입니다."),

    NO_CONTENT(204, "S003", "처리 완료", "데이터 없이 정상 처리되었습니다.");

    private final int status;
    private final String code;
    private final String title;
    private final String message;
}