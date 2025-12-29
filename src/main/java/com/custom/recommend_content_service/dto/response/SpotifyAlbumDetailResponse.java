package com.custom.recommend_content_service.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Spotify /v1/albums/{id} API 응답 DTO
 */
public record SpotifyAlbumDetailResponse(
    String id,
    String name,
    
    @JsonProperty("album_type")
    String albumType,
    
    @JsonProperty("total_tracks")
    int totalTracks,
    
    @JsonProperty("release_date")
    String releaseDate,
    
    @JsonProperty("release_date_precision")
    String releaseDatePrecision,
    
    int popularity,
    String label,
    
    @JsonProperty("is_playable")
    Boolean isPlayable,
    
    List<Image> images,
    List<Artist> artists,
    
    @JsonProperty("external_urls")
    ExternalUrls externalUrls,
    
    @JsonProperty("external_ids")
    ExternalIds externalIds,
    
    List<Copyright> copyrights,
    List<String> genres,
    
    TracksWrapper tracks
) {
    public record Image(
        String url,
        int height,
        int width
    ) {}

    public record Artist(
        String id,
        String name,
        String type,
        String uri,
        String href,
        
        @JsonProperty("external_urls")
        ExternalUrls externalUrls
    ) {}

    public record ExternalUrls(
        String spotify
    ) {}

    public record ExternalIds(
        String upc
    ) {}

    public record Copyright(
        String text,
        String type
    ) {}

    public record TracksWrapper(
        String href,
        int limit,
        String next,
        int offset,
        String previous,
        int total,
        List<TrackItem> items
    ) {}

    public record TrackItem(
        String id,
        String name,
        
        @JsonProperty("track_number")
        int trackNumber,
        
        @JsonProperty("disc_number")
        int discNumber,
        
        @JsonProperty("duration_ms")
        long durationMs,
        
        boolean explicit,
        
        @JsonProperty("preview_url")
        String previewUrl,
        
        @JsonProperty("is_playable")
        Boolean isPlayable,
        
        @JsonProperty("is_local")
        Boolean isLocal,
        
        String type,
        String uri,
        String href,
        
        @JsonProperty("external_urls")
        ExternalUrls externalUrls,
        
        List<Artist> artists
    ) {
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

    /**
     * 특정 크기의 이미지 URL 반환
     */
    public String getImageBySize(int targetHeight) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
            .filter(img -> img.height() == targetHeight)
            .findFirst()
            .map(Image::url)
            .orElse(images.get(0).url());
    }
}