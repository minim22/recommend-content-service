package com.custom.recommend_content_service.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Spotify /v1/browse/new-releases API 응답 최상위 DTO
 */
public record SpotifyNewReleasesResponse(
    Albums albums
) {
    public record Albums(
        String id,
        String href,
        int limit,
        String next,
        int offset,
        String previous,
        int total,
        List<AlbumItem> items
    ) {}

    public record AlbumItem(
        @JsonProperty("album_type")
        String albumType,
        
        @JsonProperty("total_tracks")
        int totalTracks,
        
        @JsonProperty("available_markets")
        List<String> availableMarkets,
        
        @JsonProperty("external_urls")
        ExternalUrls externalUrls,
        
        String href,
        String id,
        List<Image> images,
        String name,
        
        @JsonProperty("release_date")
        String releaseDate,
        
        @JsonProperty("release_date_precision")
        String releaseDatePrecision,
        
        String type,
        String uri,
        List<Artist> artists
    ) {
        /**
         * 대표 이미지 URL (가장 큰 이미지)
         */
        public String getLargestImageUrl() {
            if (images == null || images.isEmpty()) {
                return null;
            }
            return images.stream()
                .max((a, b) -> Integer.compare(a.height(), b.height()))
                .map(Image::url)
                .orElse(null);
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
    }

    public record ExternalUrls(
        String spotify
    ) {}

    public record Image(
        String url,
        int height,
        int width
    ) {}

    public record Artist(
        @JsonProperty("external_urls")
        ExternalUrls externalUrls,
        
        String href,
        String id,
        String name,
        String type,
        String uri
    ) {}
}