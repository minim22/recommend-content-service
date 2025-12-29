package com.custom.recommend_content_service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.custom.recommend_content_service.entity.Album;
import com.custom.recommend_content_service.entity.Track;

/**
 * 앨범 상세 응답 DTO (트랙 포함)
 */
public record AlbumDetailResponse(
    Long id,
    String albumId,
    String albumName,
    String albumType,
    String artistNames,
    String albumImageLarge,
    String albumImageMedium,
    String albumImageSmall,
    String spotifyUrl,
    String releaseDate,
    Integer totalTracks,
    Integer popularity,
    String label,
    Integer clickCnt,
    LocalDateTime createdAt,
    List<TrackResponse> tracks
) {
    public static AlbumDetailResponse from(Album album) {
        List<TrackResponse> trackResponses = album.getTracks().stream()
            .map(TrackResponse::from)
            .toList();

        return new AlbumDetailResponse(
            album.getId(),
            album.getAlbumId(),
            album.getAlbumName(),
            album.getAlbumType(),
            album.getArtistNames(),
            album.getAlbumImageLarge(),
            album.getAlbumImageMedium(),
            album.getAlbumImageSmall(),
            album.getSpotifyUrl(),
            album.getReleaseDate(),
            album.getTotalTracks(),
            album.getPopularity(),
            album.getLabel(),
            album.getClickCnt(),
            album.getCreatedAt(),
            trackResponses
        );
    }

    /**
     * 트랙 응답 DTO
     */
    public record TrackResponse(
        Long id,
        String trackId,
        String trackName,
        Integer trackNumber,
        Integer discNumber,
        Long durationMs,
        String durationFormatted,
        Boolean explicit,
        String previewUrl,
        String spotifyUrl,
        Boolean isPlayable
    ) {
        public static TrackResponse from(Track track) {
            return new TrackResponse(
                track.getId(),
                track.getTrackId(),
                track.getTrackName(),
                track.getTrackNumber(),
                track.getDiscNumber(),
                track.getDurationMs(),
                formatDuration(track.getDurationMs()),
                track.getExplicit(),
                track.getPreviewUrl(),
                track.getSpotifyUrl(),
                track.getIsPlayable()
            );
        }

        /**
         * 밀리초를 mm:ss 형식으로 변환
         */
        private static String formatDuration(Long durationMs) {
            if (durationMs == null) return "0:00";
            long totalSeconds = durationMs / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}