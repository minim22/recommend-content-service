package com.custom.recommend_content_service.dto.internal;

import com.custom.recommend_content_service.dto.response.SpotifyAlbumDetailResponse.TrackItem;

/**
 * Kafka 전송용 Track DTO
 */
public record KafkaTrackDto(
    String trackId,
    String trackName,
    int trackNumber,
    int discNumber,
    long durationMs,
    boolean explicit,
    String previewUrl,
    String spotifyUrl,
    Boolean isPlayable
) {
    /**
     * Spotify API 응답으로부터 DTO 생성
     */
    public static KafkaTrackDto from(TrackItem track) {
        return new KafkaTrackDto(
            track.id(),
            track.name(),
            track.trackNumber(),
            track.discNumber(),
            track.durationMs(),
            track.explicit(),
            track.previewUrl(),
            track.externalUrls() != null ? track.externalUrls().spotify() : null,
            track.isPlayable()
        );
    }
}