package com.custom.recommend_content_service.enums;

public interface ResultCode {
    // HTTP 상태 코드
    int getStatus();
    
    // 비즈니스 식별 코드
    String getCode();
    
    // 에러 제목
    String getTitle();
    
    // 사용자에게 보여줄 상세 메시지
    String getMessage();
}
