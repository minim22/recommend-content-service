package com.custom.recommend_content_service.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Spotify /v1/albums/{id}/tracks API 응답 DTO
 */
public record SpotifyAlbumTracksResponse(
    String href,
    int limit,
    String next,
    int offset,
    String previous,
    int total,
    List<TrackItem> items
) {
    public record TrackItem(
        List<Artist> artists,
        
        @JsonProperty("available_markets")
        List<String> availableMarkets,
        
        @JsonProperty("disc_number")
        int discNumber,
        
        @JsonProperty("duration_ms")
        long durationMs,
        
        boolean explicit,
        
        @JsonProperty("external_urls")
        ExternalUrls externalUrls,
        
        String href,
        String id,
        
        @JsonProperty("is_playable")
        Boolean isPlayable,
        
        String name,
        
        @JsonProperty("preview_url")
        String previewUrl,
        
        @JsonProperty("track_number")
        int trackNumber,
        
        String type,
        String uri
    ) {
        /**
         * 아티스트 이름들을 콤마로 연결
         */
        public String getArtistNames() {
            if (artists == null || artists.isEmpty()) {
                return "";
            }
            return artists.stream()
                .map(Artist::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }
    }

    public record Artist(
        @JsonProperty("external_urls")
        ExternalUrls externalUrls,
        
        String href,
        String id,
        String name,
        String type,
        String uri
    ) {}

    public record ExternalUrls(
        String spotify
    ) {}
}