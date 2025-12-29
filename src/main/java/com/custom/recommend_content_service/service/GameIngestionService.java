package com.custom.recommend_content_service.service;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.custom.recommend_content_service.dto.internal.KafkaGameDto;
import com.custom.recommend_content_service.dto.response.GameResponse;
import com.custom.recommend_content_service.dto.response.IgdbGameDetailResponse;
import com.custom.recommend_content_service.dto.response.IgdbIngestionResponse;
import com.custom.recommend_content_service.dto.response.TwitchTokenResponse;
import com.custom.recommend_content_service.entity.Game;
import com.custom.recommend_content_service.enums.ErrorCode;
import com.custom.recommend_content_service.exception.ApiException;
import com.custom.recommend_content_service.properties.IgdbProperties;
import com.custom.recommend_content_service.repository.GameIngestionRepository;
import com.custom.recommend_kafka.constant.TopicConstants;
import com.custom.recommend_kafka.producer.EventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class GameIngestionService {

    @Value("${igdb.client.id}")
    private String clientId;

    @Value("${igdb.client.secret}")
    private String clientSecret;

    @Value("${file.upload.path}")
    private String uploadPath;

    private final IgdbProperties igdbProperties;
    private final RestClient twitchAuthRestClient;  // Twitch OAuth용
    private final RestClient igdbRestClient;        // IGDB API용
    private final EventProducer eventProducer;
    private final GameIngestionRepository gameIngestionRepository;

    // 캐싱된 토큰
    private String accessToken;
    private Instant tokenExpiry;

    /**
     * IGDB에서 게임정보를 조회 후 Kafka에 전달하는 API
     */
    public IgdbIngestionResponse igdbPopularGames() {

        // 1. Access Token 획득 (auth.base-url 사용)
        accessToken = getAccessToken();

        // 2. IGDB API 호출 (detail.base-url 사용, Apicalypse 쿼리)
        String query = """
            fields *,
            screenshots.*;
            """;

        List<IgdbGameDetailResponse> response = igdbRestClient.post()
            .uri(uriBuilder -> {
                URI uri = uriBuilder.path("/games").build();
                log.info("IGDB API Request URL: {}", uri);
                return uri;
            })
            .header("Client-ID", clientId)
            .header("Accept: application/json")
            .header("Authorization", "Bearer " + accessToken)
            .body(query)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                log.error("IGDB API Error: status={}", res.getStatusCode());
                throw new ApiException(ErrorCode.IGDB_API_ERROR);
            })
            .body(new ParameterizedTypeReference<List<IgdbGameDetailResponse>>() {});

        // 3. 응답값 체크
        if (response == null || response.isEmpty()) {
            log.warn("IGDB Response is empty");
            // 실패 응답 반환
            throw new ApiException(ErrorCode.IGDB_API_ERROR);
        }
        log.info("response===============> {}",response);
        
        // 4. 변환 및 Kafka 전송
        List<KafkaGameDto> games = response.stream()
            .map(KafkaGameDto::from)       // 외부객체 -> 내부DTO 변환
                .peek(this::sendToKafka)   // Kafka 전송 (Side Effect)
                .toList();                 // 리스트로 수집

        log.info("Successfully ingested {} games.", games.size());

        return new IgdbIngestionResponse(
            games.size()
        );
    }

    /**
     * Kafka Consumer - 게임 정보 배치 처리
     */
    @KafkaListener(
        topics = TopicConstants.GAME_INGESTION_TOPIC,
        groupId = "content-service-group",
        containerFactory = "batchFactory"
    )
    @Transactional
    public void consumeGameBatch(List<KafkaGameDto> games) {
        for (KafkaGameDto kafkaGameDto : games) {
            try {
                validateGameDto(kafkaGameDto);

                Optional<Game> existingGame = gameIngestionRepository.findById(kafkaGameDto.id());
                
                // 이미지 다운로드 및 저장
                String savedImagePath = downloadAndSaveImage(
                    kafkaGameDto.screenshotUrl(),
                    accessToken
                );
                
                if (existingGame.isPresent()) {
                    Game game = existingGame.get();
                    game.update(
                        kafkaGameDto.name(),
                        kafkaGameDto.summary(),
                        kafkaGameDto.slug(),
                        savedImagePath
                    );
                    log.info("Updated game: id={},buildScreenshotUrl ={}", game.getId(), kafkaGameDto.screenshotUrl());
                } else {
                    Game game = Game.create(
                        kafkaGameDto.id(),
                        kafkaGameDto.name(),
                        kafkaGameDto.slug(),
                        kafkaGameDto.summary(),
                        kafkaGameDto.clickCnt(),
                        kafkaGameDto.createdAt(),
                        savedImagePath
                    );
                    gameIngestionRepository.save(game);
                    log.info("Created new game: id={}, buildScreenshotUrl ={}", game.getId(), kafkaGameDto.screenshotUrl());
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid game data: id={}, error={}", kafkaGameDto.id(), e.getMessage());
            } catch (Exception e) {
                log.error("Failed to save game: id={}", kafkaGameDto.id(), e);
                throw e;
            }
        }
        log.info("Batch processed {} games", games.size());
    }

    /**
     * IGDB에서 이미지 다운로드 및 파일 시스템에 저장
     */
    private String downloadAndSaveImage(String imageId, String accessToken) {
        
        if (imageId == null || imageId.isBlank()) {
            log.warn("ImageId is null or empty");
            return null;
        }
        
        try {
            // 이미지 URL 생성
            String imageUrl = buildScreenshotUrl(imageId);

            log.info("Downloading image from: {}", imageUrl);
            
            // 이미지 다운로드
            byte[] imageBytes = RestClient.create()
                .get()
                .uri(imageUrl)
                .header("Client-ID", clientId)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(byte[].class);
            
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Failed to download image: {}", imageId);
                return null;
            }
            
            // 프로젝트 루트 디렉토리 기준으로 경로 생성
            String projectRoot = System.getProperty("user.dir");
            java.nio.file.Path uploadDir = java.nio.file.Paths.get(projectRoot, uploadPath);
            
            // 상대 경로인 경우 프로젝트 루트 기준으로 변환
            if (!uploadDir.isAbsolute()) {
                uploadDir = java.nio.file.Paths.get(System.getProperty("user.dir"), uploadPath);
            }
            
            // 디렉토리 없으면 생성
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
                log.info("Created upload directory: {}", uploadDir.toAbsolutePath());
            }
            
            // 파일 저장
            String filename = imageId + ".jpg";
            java.nio.file.Path filePath = uploadDir.resolve(filename);
            java.nio.file.Files.write(filePath, imageBytes);
            
            log.info("Image saved successfully: {}, size: {} bytes", 
                    filePath.toAbsolutePath(), imageBytes.length);
            
            // 웹에서 접근 가능한 경로 반환
            return "/images/games/" + filename;
            
        } catch (IOException e) {
            log.error("IO error while saving image: {}", imageId, e);
            return null;
        } catch (Exception e) {
            log.error("Failed to download/save image: {}", imageId, e);
            return null;
        }
    }

    /**
     * 메인에 노출될 게임정보 조회 API
     */
    public List<GameResponse> getIngestPopularGames() {
        List<Game> gameList = gameIngestionRepository.findAllByOrderByClickCntDesc();
        return gameList.stream()
                .map(GameResponse::from)
                .toList();
    }

    /**
     * Twitch OAuth2 토큰 획득 (auth.base-url 사용)
     */
    private String getAccessToken() {
        // 캐싱된 토큰이 유효하면 재사용
        if (accessToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return accessToken;
        }

        // twitchAuthRestClient는 auth.base-url (https://id.twitch.tv/oauth2/token)을 사용
        TwitchTokenResponse tokenResponse = twitchAuthRestClient.post()
        .uri(uriBuilder -> {
            URI uri = uriBuilder
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("grant_type", "client_credentials")
                    .build();
            log.info("Twitch Auth Request URL: {}", uri);
            return uri;
        })
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
            log.info("Twitch Auth Error: status={}", res.getStatusCode());
            throw new ApiException(ErrorCode.TWITCH_AUTH_ERROR);
        })
        .body(TwitchTokenResponse.class);

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new ApiException(ErrorCode.TWITCH_AUTH_ERROR);
        }

        // 토큰 캐싱 (만료 1분 전까지 유효)
        accessToken = tokenResponse.accessToken();
        tokenExpiry = Instant.now().plusSeconds(tokenResponse.expiresIn() - 60);

        log.info("Twitch access token acquired, expires in {} seconds", tokenResponse.expiresIn());
        return accessToken;
    }

    /**
     * Kafka 전송 로직
     */
    private void sendToKafka(KafkaGameDto kafkaGameDto) {
        try {
            eventProducer.send(
                TopicConstants.GAME_INGESTION_TOPIC,
                String.valueOf(kafkaGameDto.id()),
                kafkaGameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game to Kafka: id={}", kafkaGameDto.id(), e);
            throw new ApiException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private void validateGameDto(KafkaGameDto gameDto) {
        if (gameDto == null) {
            throw new IllegalArgumentException("GameDto cannot be null");
        }
        if (gameDto.id() <= 0) {
            throw new IllegalArgumentException("Invalid game id: " + gameDto.id());
        }
        if (gameDto.name() == null || gameDto.name().isBlank()) {
            throw new IllegalArgumentException("Game name cannot be empty");
        }
    }

    /**
     * 스크린샷 URL 생성 (image.base-url 사용)
     */
    private String buildScreenshotUrl(String imageId) {
        if (imageId == null || imageId.isBlank()) {
            return null;
        }
        // igdbProperties의 imageUrl과 imageId 조합
        return igdbProperties.imageUrl() + "/" + imageId + ".jpg";
    }

    /**
     * 게임 클릭 수 증가
     */
    @Transactional
    public void incrementClickCount(Long gameId) {
        Game game = gameIngestionRepository.findById(gameId)
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND));
        
        game.incrementClickCount();
        
        log.info("Game click count incremented: id={}, newCount={}", gameId, game.getClickCnt());
    }
}
