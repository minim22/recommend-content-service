package com.custom.recommend_content_service.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "track")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "track_id", unique = true, nullable = false)
    private String trackId;

    @Column(name = "track_name", nullable = false)
    private String trackName;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(name = "disc_number")
    private Integer discNumber;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "explicit")
    private Boolean explicit;

    @Column(name = "preview_url", length = 500)
    private String previewUrl;

    @Column(name = "spotify_url", length = 500)
    private String spotifyUrl;

    @Column(name = "is_playable")
    private Boolean isPlayable;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Track 엔티티 생성
     */
    public static Track create(
        String trackId,
        String trackName,
        Integer trackNumber,
        Integer discNumber,
        Long durationMs,
        Boolean explicit,
        String previewUrl,
        String spotifyUrl,
        Boolean isPlayable
    ) {
        return Track.builder()
            .trackId(trackId)
            .trackName(trackName)
            .trackNumber(trackNumber)
            .discNumber(discNumber)
            .durationMs(durationMs)
            .explicit(explicit)
            .previewUrl(previewUrl)
            .spotifyUrl(spotifyUrl)
            .isPlayable(isPlayable)
            .build();
    }

    /**
     * Track 엔티티 업데이트
     */
    public void update(
        String trackName,
        String previewUrl,
        Boolean isPlayable
    ) {
        this.trackName = trackName;
        this.previewUrl = previewUrl;
        this.isPlayable = isPlayable;
    }
}