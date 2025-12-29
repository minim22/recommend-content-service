package com.custom.recommend_content_service.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "album")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "album_id", unique = true, nullable = false)
    private String albumId;

    @Column(name = "album_name", nullable = false)
    private String albumName;

    @Column(name = "album_type")
    private String albumType;

    @Column(name = "artist_names")
    private String artistNames;

    @Column(name = "album_image_large", length = 500)
    private String albumImageLarge;

    @Column(name = "album_image_medium", length = 500)
    private String albumImageMedium;

    @Column(name = "album_image_small", length = 500)
    private String albumImageSmall;

    @Column(name = "spotify_url", length = 500)
    private String spotifyUrl;

    @Column(name = "release_date")
    private String releaseDate;

    @Column(name = "total_tracks")
    private Integer totalTracks;

    @Column(name = "popularity")
    private Integer popularity;

    @Column(name = "label")
    private String label;

    @Column(name = "click_cnt")
    @Builder.Default
    private Integer clickCnt = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Track> tracks = new ArrayList<>();

    /**
     * Album 엔티티 생성
     */
    public static Album create(
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
        String label
    ) {
        return Album.builder()
            .albumId(albumId)
            .albumName(albumName)
            .albumType(albumType)
            .artistNames(artistNames)
            .albumImageLarge(albumImageLarge)
            .albumImageMedium(albumImageMedium)
            .albumImageSmall(albumImageSmall)
            .spotifyUrl(spotifyUrl)
            .releaseDate(releaseDate)
            .totalTracks(totalTracks)
            .popularity(popularity)
            .label(label)
            .clickCnt(0)
            .tracks(new ArrayList<>())
            .build();
    }

    /**
     * Album 엔티티 업데이트
     */
    public void update(
        String albumName,
        String artistNames,
        String albumImageLarge,
        String spotifyUrl,
        Integer popularity
    ) {
        this.albumName = albumName;
        this.artistNames = artistNames;
        this.albumImageLarge = albumImageLarge;
        this.spotifyUrl = spotifyUrl;
        this.popularity = popularity;
    }

    /**
     * 트랙 추가
     */
    public void addTrack(Track track) {
        this.tracks.add(track);
        track.setAlbum(this);
    }

    /**
     * 트랙 전체 교체
     */
    public void replaceTracks(List<Track> newTracks) {
        this.tracks.clear();
        for (Track track : newTracks) {
            addTrack(track);
        }
    }

    /**
     * 클릭 수 증가
     */
    public void incrementClickCount() {
        if (this.clickCnt == null) {
            this.clickCnt = 0;
        }
        this.clickCnt++;
    }
}