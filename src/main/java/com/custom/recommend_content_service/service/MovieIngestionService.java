package com.custom.recommend_content_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.custom.recommend_content_service.dto.internal.MovieDto;
import com.custom.recommend_content_service.dto.response.IngestionResponse;
import com.custom.recommend_content_service.dto.response.TmdbMovieResponse;
import com.custom.recommend_content_service.enums.ErrorCode;
import com.custom.recommend_content_service.exception.ApiException;
import com.custom.recommend_kafka.constant.TopicConstants;
import com.custom.recommend_kafka.producer.EventProducer;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieIngestionService {

    private final RestClient tmdbRestClient;
    private final EventProducer eventProducer;

    /**
     * Tmdb에서 영화정보를 조회후 Kafka에 전달 하는 API
     * @return
     */
    public IngestionResponse ingestPopularMovies() {

        // 1. TMDB API 호출
        TmdbMovieResponse response = tmdbRestClient.get()
                .uri("/movie/popular?language=en-US&page=1")
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                    log.error("TMDB API Error: status={}", res.getStatusCode());
                })
                .body(TmdbMovieResponse.class);

        // 2.
        if (response == null || response.results() == null) {
            log.warn("TMDB Response is empty");
            // 실패 응답 반환
            throw new ApiException(ErrorCode.TMDB_API_ERROR);
        }
        
        // 2. 변환 및 Kafka 전송 (Stream 파이프라인)
        List<MovieDto> movies = response.results().stream()
                .map(MovieDto::from)       // 외부객체 -> 내부DTO 변환
                .peek(this::sendToKafka)   // Kafka 전송 (Side Effect)
                .toList();                 // 리스트로 수집

        log.info("Successfully ingested {} movies.", movies.size());

        return new IngestionResponse(
            movies.size()
        );
    }

    /**
     * Kafka 전송 로직 (Stream 내부에서 호출됨)
     */
    private void sendToKafka(MovieDto movieDto) {
        eventProducer.send(
                TopicConstants.MOVIE_INGESTION_TOPIC,
                String.valueOf(movieDto.id()),
                movieDto
        );
    }
}
