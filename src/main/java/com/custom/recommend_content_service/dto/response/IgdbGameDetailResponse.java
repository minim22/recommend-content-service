package com.custom.recommend_content_service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 실제 IGDB에서 받아오는 항목값 */
public record IgdbGameDetailResponse(

    @JsonProperty("id")
    Long id,

    @JsonProperty("name")
    String name,

    @JsonProperty("slug")
    String slug,

    @JsonProperty("summary")
    String summary,

    @JsonProperty("createdAt")
    LocalDateTime createdAt,

    @JsonProperty("screenshots")
    List<Screenshot> screenshots
) {
    public record Screenshot(
        Long id,
        @JsonProperty("image_id")
        String imageId,
        String url
    ) {}
    
    // 첫 번째 스크린샷의 image_id 가져오기
    public String getFirstScreenshotImageId() {
        if (screenshots != null && !screenshots.isEmpty()) {
            return screenshots.get(0).imageId();  // image_id만 반환
        }
        return null;
    }
}