package com.custom.recommend_content_service.dto.internal;

import java.util.List;

import com.custom.recommend_content_service.dto.response.SpotifyAlbumDetailResponse;
import com.custom.recommend_content_service.dto.response.SpotifyAlbumDetailResponse.Image;

/**
 * Kafka 전송용 Album DTO
 */
public record KafkaAlbumDto(
    String albumId,
    String albumName,
    String albumType,
    String artistNames,
    String albumImageLarge,
    String albumImageMedium,
    String albumImageSmall,
    String spotifyUrl,
    String releaseDate,
    int totalTracks,
    int popularity,
    String label,
    List<KafkaTrackDto> tracks
) {
    /**
     * Spotify API 응답으로부터 DTO 생성
     */
    public static KafkaAlbumDto from(SpotifyAlbumDetailResponse response) {
        // tracks null 체크
        List<KafkaTrackDto> trackDtos = List.of();
        if (response.tracks() != null && response.tracks().items() != null) {
            trackDtos = response.tracks().items().stream()
                .map(KafkaTrackDto::from)
                .toList();
        }

        return new KafkaAlbumDto(
            response.id(),
            response.name(),
            response.albumType(),
            response.getArtistNames(),
            getImageBySize(response.images(), 640),
            getImageBySize(response.images(), 300),
            getImageBySize(response.images(), 64),
            response.externalUrls() != null ? response.externalUrls().spotify() : null,
            response.releaseDate(),
            response.totalTracks(),
            response.popularity(),
            response.label(),
            trackDtos
        );
    }

    private static String getImageBySize(List<Image> images, int targetHeight) {
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