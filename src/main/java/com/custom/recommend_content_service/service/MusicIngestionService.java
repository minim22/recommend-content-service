package com.custom.recommend_content_service.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.custom.recommend_content_service.dto.internal.KafkaAlbumDto;
import com.custom.recommend_content_service.dto.internal.KafkaTrackDto;
import com.custom.recommend_content_service.dto.response.AlbumDetailResponse;
import com.custom.recommend_content_service.dto.response.AlbumResponse;
import com.custom.recommend_content_service.dto.response.SpotifyAlbumDetailResponse;
import com.custom.recommend_content_service.dto.response.SpotifyIngestionResponse;
import com.custom.recommend_content_service.dto.response.SpotifyNewReleasesResponse;
import com.custom.recommend_content_service.dto.response.SpotifyNewReleasesResponse.AlbumItem;
import com.custom.recommend_content_service.dto.response.SpotifyTokenResponse;
import com.custom.recommend_content_service.entity.Album;
import com.custom.recommend_content_service.entity.Track;
import com.custom.recommend_content_service.enums.ErrorCode;
import com.custom.recommend_content_service.exception.ApiException;
import com.custom.recommend_content_service.repository.AlbumIngestionRepository;
import com.custom.recommend_kafka.constant.TopicConstants;
import com.custom.recommend_kafka.producer.EventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MusicIngestionService {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    private final RestClient spotifyAuthRestClient;
    private final RestClient spotifyApiRestClient;
    private final EventProducer eventProducer;
    private final AlbumIngestionRepository albumIngestionRepository;
    private final ObjectMapper objectMapper;

    // 캐싱된 토큰
    private String accessToken;
    private Instant tokenExpiry;

    /**
     * Spotify에서 신규 앨범 정보를 조회 후 Kafka에 전달 (배치용)
     */
    public SpotifyIngestionResponse spotifyPopularMusics() {
        
        // 1. Access Token 획득
        String token = getAccessToken();

        // 2. 신규 앨범 목록 조회
        SpotifyNewReleasesResponse response = fetchNewReleases(token);

        try {
            String json = objectMapper.writeValueAsString(response);
            log.info("=========> newReleases: {}", json);
        } catch (Exception e) {
            log.error("Failed to convert to JSON", e);
        }

        if (response == null || response.albums() == null) {
            log.warn("Spotify new releases response is empty");
            throw new ApiException(ErrorCode.SPOTIFY_API_ERROR);
        }

        // 3. 각 앨범 상세 조회 (트랙 포함) 및 Kafka 전송
        List<KafkaAlbumDto> albums = new ArrayList<>();

        for (AlbumItem albumItem : response.albums().items()) {
            try {
                // available_markets 체크
                if (albumItem.availableMarkets() == null || albumItem.availableMarkets().isEmpty()) {
                    log.warn("Skipping album with no available markets: id={}, name={}",
                        albumItem.id(), albumItem.name());
                    continue;
                }

                // 앨범 상세 조회 (트랙 포함)
                SpotifyAlbumDetailResponse albumDetail = fetchAlbumDetail(token, albumItem.id());

                if (albumDetail == null) {
                    continue;
                }

                // DTO 변환 및 Kafka 전송
                KafkaAlbumDto dto = KafkaAlbumDto.from(albumDetail);
                sendToKafka(dto);
                albums.add(dto);

            } catch (Exception e) {
                log.error("Failed to process album: id={}, name={}", albumItem.id(), albumItem.name(), e);
                // 개별 앨범 실패는 무시하고 계속 진행
            }
        }

        log.info("Successfully ingested {} albums.", albums.size());
        return new SpotifyIngestionResponse(albums.size());
    }

    /**
     * 신규 앨범 목록 조회
     */
    private SpotifyNewReleasesResponse fetchNewReleases(String token) {
        return spotifyApiRestClient.get()
            .uri(uriBuilder -> {
                URI uri = uriBuilder
                    .path("/browse/new-releases")
                    .queryParam("limit", 20)
                    .queryParam("offset", 10)
                    .build();
                log.info("Spotify New-releases Request URL: {}", uri);
                return uri;
            })
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                log.error("Spotify API Error: status={}", res.getStatusCode());
                throw new ApiException(ErrorCode.SPOTIFY_API_ERROR);
            })
            .body(SpotifyNewReleasesResponse.class);
    }

    /**
     * 앨범 상세 조회 (트랙 포함)
     */
    private SpotifyAlbumDetailResponse fetchAlbumDetail(String token, String albumId) {
        try {
            SpotifyAlbumDetailResponse response = spotifyApiRestClient.get()
                .uri(uriBuilder -> {
                    URI uri = uriBuilder
                        .path("/albums/{albumId}")
                        .queryParam("market", "ES")
                        .build(albumId);
                    log.info("Spotify Album Detail Request URL: {}", uri);
                    return uri;
                })
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                    log.warn("Skipping album (4xx): id={}, status={}", albumId, res.getStatusCode());
                })
                .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                    log.error("Spotify server error: id={}, status={}", albumId, res.getStatusCode());
                    throw new ApiException(ErrorCode.SPOTIFY_API_ERROR);
                })
                .body(SpotifyAlbumDetailResponse.class);

            if (response != null) {
                log.info("Fetched album detail: id={}, name={}, tracks={}",
                    response.id(), response.name(),
                    response.tracks() != null ? response.tracks().items().size() : 0);
            }

            return response;
        } catch (Exception e) {
            log.warn("Failed to fetch album detail: id={}", albumId, e);
            return null;
        }
    }

    /**
     * Spotify OAuth2 토큰 획득
     */
    private String getAccessToken() {
        // 캐싱된 토큰이 유효하면 재사용
        if (accessToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return accessToken;
        }

        // Basic Auth 헤더 생성
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");

        SpotifyTokenResponse tokenResponse = spotifyAuthRestClient.post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", "Basic " + encodedAuth)
            .body(formData)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                log.error("Spotify Auth Error: status={}", res.getStatusCode());
                throw new ApiException(ErrorCode.SPOTIFY_AUTH_ERROR);
            })
            .body(SpotifyTokenResponse.class);

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new ApiException(ErrorCode.SPOTIFY_AUTH_ERROR);
        }

        // 토큰 캐싱 (만료 10초 전에 갱신하도록 설정)
        this.accessToken = tokenResponse.accessToken();
        this.tokenExpiry = Instant.now().plusSeconds(tokenResponse.expiresIn() - 10);

        log.info("Spotify access token acquired, accessToken in {} expires in {} seconds", accessToken, tokenResponse.expiresIn());
        return this.accessToken;
    }

    /**
     * Kafka 전송
     */
    private void sendToKafka(KafkaAlbumDto dto) {
        try {
            eventProducer.send(
                TopicConstants.MUSIC_INGESTION_TOPIC,
                dto.albumId(),
                dto
            );
            log.debug("Sent to Kafka: albumId={}, name={}, tracks={}",
                dto.albumId(), dto.albumName(), dto.tracks().size());
        } catch (Exception e) {
            log.error("Failed to send album to Kafka: albumId={}", dto.albumId(), e);
            throw new ApiException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * Kafka Consumer - 앨범 + 트랙 정보 배치 처리
     */
    @KafkaListener(
        topics = TopicConstants.MUSIC_INGESTION_TOPIC,
        groupId = "content-service-group",
        containerFactory = "batchFactory"
    )
    @Transactional
    public void consumeAlbumBatch(List<KafkaAlbumDto> albums) {
        for (KafkaAlbumDto dto : albums) {
            try {
                validateAlbumDto(dto);
                processAlbum(dto);
            } catch (IllegalArgumentException e) {
                log.error("Invalid album data: albumId={}, error={}", dto.albumId(), e.getMessage());
            } catch (Exception e) {
                log.error("Failed to save album: albumId={}", dto.albumId(), e);
                throw e;
            }
        }
        log.info("Batch processed {} albums", albums.size());
    }

    /**
     * 앨범 처리 (저장 또는 업데이트)
     */
    private void processAlbum(KafkaAlbumDto dto) {
        Optional<Album> existingAlbum = albumIngestionRepository.findByAlbumId(dto.albumId());

        if (existingAlbum.isPresent()) {
            // 기존 앨범 업데이트
            Album album = existingAlbum.get();
                album.update(
                    dto.albumName(),
                    dto.artistNames(),
                    dto.albumImageLarge(),
                    dto.spotifyUrl(),
                    dto.popularity()
                );

            // 트랙 업데이트
            List<Track> newTracks = dto.tracks().stream()
                .map(this::createTrackEntity)
                .toList();
            album.replaceTracks(newTracks);

            log.info("Updated album: albumId={}, name={}, tracks={}",
                album.getAlbumId(), dto.albumName(), dto.tracks().size());
        } else {
            // 새 앨범 생성
            Album album = Album.create(
                dto.albumId(),
                dto.albumName(),
                dto.albumType(),
                dto.artistNames(),
                dto.albumImageLarge(),
                dto.albumImageMedium(),
                dto.albumImageSmall(),
                dto.spotifyUrl(),
                dto.releaseDate(),
                dto.totalTracks(),
                dto.popularity(),
                dto.label()
            );

            // 트랙 추가
            for (KafkaTrackDto trackDto : dto.tracks()) {
                Track track = createTrackEntity(trackDto);
                album.addTrack(track);
            }

            albumIngestionRepository.save(album);
            log.info("Created new album: albumId={}, name={}, tracks={}",
                album.getAlbumId(), dto.albumName(), dto.tracks().size());
        }
    }

    /**
     * Track 엔티티 생성
     */
    private Track createTrackEntity(KafkaTrackDto dto) {
        return Track.create(
            dto.trackId(),
            dto.trackName(),
            dto.trackNumber(),
            dto.discNumber(),
            dto.durationMs(),
            dto.explicit(),
            dto.previewUrl(),
            dto.spotifyUrl(),
            dto.isPlayable()
        );
    }

    /**
     * Album DTO 유효성 검증
     */
    private void validateAlbumDto(KafkaAlbumDto dto) {
        if (dto.albumId() == null || dto.albumId().isBlank()) {
            throw new IllegalArgumentException("Album ID is required");
        }
        if (dto.albumName() == null || dto.albumName().isBlank()) {
            throw new IllegalArgumentException("Album name is required");
        }
    }

    /**
     * 메인 페이지용 앨범 목록 조회 (클릭수 순)
     */
    public List<AlbumResponse> getIngestPopularMusics() {
        List<Album> albums = albumIngestionRepository.findTop20ByOrderByClickCntDesc();
        return albums.stream()
            .map(AlbumResponse::from)
            .toList();
    }

    /**
     * 최신 앨범 목록 조회
     */
    public List<AlbumResponse> getLatestAlbums() {
        List<Album> albums = albumIngestionRepository.findTop20ByOrderByCreatedAtDesc();
        return albums.stream()
            .map(AlbumResponse::from)
            .toList();
    }

    /**
     * 인기 앨범 목록 조회 (Spotify popularity 기준)
     */
    public List<AlbumResponse> getPopularAlbums() {
        List<Album> albums = albumIngestionRepository.findTop20ByOrderByPopularityDesc();
        return albums.stream()
            .map(AlbumResponse::from)
            .toList();
    }

    /**
     * 앨범 상세 조회 (트랙 포함)
     */
    public AlbumDetailResponse getAlbumDetail(Long id) {
        Album album = albumIngestionRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND));
        return AlbumDetailResponse.from(album);
    }

    /**
     * 앨범 상세 조회 by Spotify Album ID
     */
    public AlbumDetailResponse getAlbumDetailBySpotifyId(String albumId) {
        Album album = albumIngestionRepository.findByAlbumId(albumId)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND));
        return AlbumDetailResponse.from(album);
    }

    /**
     * 앨범 클릭수 증가
     */
    @Transactional
    public void incrementClickCount(Long id) {
        Album album = albumIngestionRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND));

        album.incrementClickCount();

        log.info("Album click count incremented: id={}, newCount={}", id, album.getClickCnt());
    }
}