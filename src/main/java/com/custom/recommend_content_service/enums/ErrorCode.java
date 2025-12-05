package com.custom.recommend_content_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResultCode{

    SYSTEM_ERROR(500, "E999", "시스템 오류", "일시적인 오류가 발생했습니다. 관리자에게 문의하세요."),

    INVALID_INPUT(400, "E001", "입력값 오류", "요청하신 입력값이 올바르지 않습니다."),
    MISSING_PARAMETER(400, "E002", "파라미터 누락", "필수 정보가 누락되었습니다."),

    UNAUTHORIZED(401, "E401", "인증 실패", "로그인이 필요합니다."),
    ACCESS_DENIED(403, "E403", "권한 없음", "해당 기능에 대한 접근 권한이 없습니다."),

    TMDB_API_ERROR(502, "E100", "외부 연동 실패", "영화 정보를 가져오는 중 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String title;
    private final String message;
}