package com.custom.recommend_content_service.dto.response;

import java.time.LocalDateTime;

import com.custom.recommend_content_service.entity.Album;

public record AlbumResponse(
    Long id,
    String alblumId,
    String albumName,
    String albumType,
    String artistNames,
    String albumImageLarge,
    String albumImageMedium,
    String albumImageSmall,
    String releaseDate,
    String spotifyUrl,
    Integer clickCnt,
    LocalDateTime createdAt
) {
    public static AlbumResponse from(Album album) {
        return new AlbumResponse(
            album.getId(),
            album.getAlbumId(),
            album.getAlbumName(),
            album.getAlbumType(),
            album.getArtistNames(),
            album.getAlbumImageLarge(),
            album.getAlbumImageMedium(),
            album.getAlbumImageSmall(),
            album.getReleaseDate(),
            album.getSpotifyUrl(),
            album.getClickCnt(),
            album.getCreatedAt()
        );
    }
}