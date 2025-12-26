package com.custom.recommend_content_service.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.custom.recommend_content_service.dto.internal.KafkaGameDto;
import com.custom.recommend_content_service.dto.response.GameResponse;
import com.custom.recommend_content_service.dto.response.SpotifyIngestionResponse;
import com.custom.recommend_content_service.dto.response.SpotifyResponse;
import com.custom.recommend_content_service.dto.response.SpotifyTokenResponse;
import com.custom.recommend_content_service.entity.Music;
import com.custom.recommend_content_service.enums.ErrorCode;
import com.custom.recommend_content_service.exception.ApiException;
import com.custom.recommend_content_service.repository.SpotifyIngestionRepository;
import com.custom.recommend_kafka.constant.TopicConstants;
import com.custom.recommend_kafka.producer.EventProducer;
import com.custom.recommend_content_service.properties.SpotifyProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class MusicIngestionService {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    private final SpotifyProperties spotifyProperties;
    private final RestClient spotifyAuthRestClient;
    private final RestClient spotifyApiRestClient;
    private final EventProducer eventProducer;
    private final SpotifyIngestionRepository spotifyIngestionRepository;

    // 캐싱된 토큰
    private String accessToken;
    private Instant tokenExpiry;

    /**
     * Spotify에서 음악정보를 조회후 Kafka에 전달 하는 API
     * @return
     */
    public SpotifyIngestionResponse spotifyPopularMusics() {

        // 1. Access Token 획득 (auth.base-url 사용)
        accessToken = getAccessToken();

        // 2. Spotify API 호출 - 추천 트랙 가져오기
        SpotifyResponse response = spotifyApiRestClient.get()
                .uri("/recommendations?seed_genres=pop,rock,hip-hop&limit=20")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                    log.error("Spotify API Error: status={}", res.getStatusCode());
                    throw new ApiException(ErrorCode.SPOTIFY_API_ERROR);
                })
                .body(SpotifyResponse.class);

        if (response == null || response.tracks() == null || response.tracks().isEmpty()) {
            log.warn("Spotify Response is empty");
            throw new ApiException(ErrorCode.SPOTIFY_API_ERROR);
        }

        log.info("Successfully fetched {} tracks from Spotify", response.tracks().size());
        return response.tracks();
    }

    public List<GameResponse> getIngestPopularMusics() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 음악 클릭 수 증가
     */
    @Transactional
    public void incrementClickCount(Long musicId) {
        Music music = spotifyIngestionRepository.findById(musicId)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND));
        
        music.incrementClickCount();
        
        log.info("Movie click count incremented: id={}, newCount={}", musicId, music.getClickCnt());
    }

    /**
     * Spotify OAuth2 토큰 획득 (auth.base-url 사용)
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

        // spotifyAuthRestClient는 auth.base-url (https://accounts.spotify.com/api/token)을 사용
        SpotifyTokenResponse tokenResponse = spotifyAuthRestClient.post()
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
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

        log.info("Spotify access token acquired, expires in {} seconds", tokenResponse.expiresIn());
        return tokenResponse.accessToken();
    }

    /**
     * Kafka 전송 로직
     */
    private void sendToKafka(KafkaGameDto kafkaGameDto) {
        try {
            eventProducer.send(
                TopicConstants.MUSIC_INGESTION_TOPIC,
                String.valueOf(kafkaGameDto.id()),
                kafkaGameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game to Kafka: id={}", kafkaGameDto.id(), e);
            throw new ApiException(ErrorCode.SYSTEM_ERROR);
        }
    }
}