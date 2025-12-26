package com.custom.recommend_content_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TwitchTokenResponse(

    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("expires_in")
    Long expiresIn
){}