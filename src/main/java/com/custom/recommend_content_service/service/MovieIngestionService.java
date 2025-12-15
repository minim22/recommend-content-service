package com.custom.recommend_content_service.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.custom.recommend_content_service.dto.internal.KafkaMovieDto;
import com.custom.recommend_content_service.dto.response.MovieResponse;
import com.custom.recommend_content_service.dto.response.TmdbIngestionResponse;
import com.custom.recommend_content_service.dto.response.TmdbMovieResponse;
import com.custom.recommend_content_service.entity.Movie;
import com.custom.recommend_content_service.enums.ErrorCode;
import com.custom.recommend_content_service.exception.ApiException;
import com.custom.recommend_content_service.repository.MovieIngestionRepository;
import com.custom.recommend_kafka.constant.TopicConstants;
import com.custom.recommend_kafka.producer.EventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class MovieIngestionService {

    @Value("${tmdb.default.language:en-US}")
    private String defaultLanguage;
    
    @Value("${tmdb.default.pages:1}")
    private int defaultPages;

    @Value("${tmdb.image.path}")
    private String tmdbImagePath;

    private final RestClient tmdbRestClient;
    private final EventProducer eventProducer;
    private final MovieIngestionRepository movieIngestionRepository;

    /**
     * Tmdb에서 영화정보를 조회후 Kafka에 전달 하는 API
     * @return
     */
    public TmdbIngestionResponse tmdbPopularMovies() {

        // 1. TMDB API 호출
        TmdbMovieResponse response = tmdbRestClient.get()
                .uri("/movie/popular?language="+defaultLanguage+"&page="+defaultPages)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                    log.error("TMDB API Error: status={}", res.getStatusCode());
                })
                .body(TmdbMovieResponse.class);

        // 2. 응답값 체크
        if (response == null || response.results() == null) {
            log.warn("TMDB Response is empty");
            // 실패 응답 반환
            throw new ApiException(ErrorCode.TMDB_API_ERROR);
        }
        
        // 2. 변환 및 Kafka 전송 (Stream 파이프라인)
        List<KafkaMovieDto> movies = response.results().stream()
                .map(KafkaMovieDto::from)       // 외부객체 -> 내부DTO 변환
                .peek(this::sendToKafka)   // Kafka 전송 (Side Effect)
                .toList();                 // 리스트로 수집

        log.info("Successfully ingested {} movies.", movies.size());

        return new TmdbIngestionResponse(
            movies.size()
        );
    }

    @KafkaListener(
        topics = TopicConstants.MOVIE_INGESTION_TOPIC,
        groupId = "content-service-group",
        containerFactory = "batchFactory"
    )
    @Transactional
    public void consumeMovieBatch(List<KafkaMovieDto> movies) {
        for (KafkaMovieDto kafkaMovieDto : movies) {
            try {
                // 1. 데이터 검증
                validateMovieDto(kafkaMovieDto);
                
                Optional<Movie> existingMovie = movieIngestionRepository.findById(kafkaMovieDto.id());

                LocalDate releaseDate = parseReleaseDate(kafkaMovieDto.releaseDate());
                String posterPath = tmdbImagePath + kafkaMovieDto.posterPath();

                // 2. 중복 체크 및 저장
                if (existingMovie.isPresent()) {
                    Movie movie = existingMovie.get();
                    movie.update(
                        kafkaMovieDto.title(),
                        kafkaMovieDto.overview(),
                        kafkaMovieDto.genreIds(),
                        kafkaMovieDto.voteAverage(),
                        kafkaMovieDto.popularity(),
                        releaseDate,
                        posterPath
                    );
                    log.info("Updated movie: id={}", movie.getId());
                } else {
                    Movie movie = Movie.create(
                        kafkaMovieDto.id(),
                        kafkaMovieDto.title(),
                        kafkaMovieDto.overview(),
                        kafkaMovieDto.genreIds(),
                        kafkaMovieDto.voteAverage(),
                        kafkaMovieDto.popularity(),
                        releaseDate,
                        posterPath
                    );
                    movieIngestionRepository.save(movie);
                    log.info("Created new movie: id={}", movie.getId());
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid movie data: id={}, error={}", kafkaMovieDto.id(), e.getMessage());
            } catch (Exception e) {
                log.error("Failed to save movie: id={}", kafkaMovieDto.id(), e);
                throw e;
            }
        }
        log.info("Batch processed {} movies", movies.size());
    }

    /**
     * 메인에 노출될 영화정보를 조회 하는 API
     * @return
     */
    public List<MovieResponse> getIngestPopularMovies() {

        List<Movie> movieList = movieIngestionRepository.findAll();

         return movieList.stream()
            .map(MovieResponse::from)
            .toList();
        }

    /**
     * Kafka 전송 로직 (Stream 내부에서 호출됨)
    */
    private void sendToKafka(KafkaMovieDto kafkaMovieDto) {
        try {
            eventProducer.send(
                TopicConstants.MOVIE_INGESTION_TOPIC,
                String.valueOf(kafkaMovieDto.id()),
                kafkaMovieDto
            );
        } catch (Exception e) {
            log.error("Failed to send movie to Kafka: id={}", kafkaMovieDto.id(), e);
            throw new ApiException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private void validateMovieDto(KafkaMovieDto movieDto) {
        if (movieDto == null) {
            throw new IllegalArgumentException("MovieDto cannot be null");
        }
        if (movieDto.id() <= 0) {
            throw new IllegalArgumentException("Invalid movie id: " + movieDto.id());
        }
        if (movieDto.title() == null || movieDto.title().isBlank()) {
            throw new IllegalArgumentException("Movie title cannot be empty");
        }
    }

    private LocalDate parseReleaseDate(String releaseDateStr) {
        if (releaseDateStr == null || releaseDateStr.isBlank()) {
            log.warn("Release date is null or empty, using default date");
            return LocalDate.of(1900, 1, 1);  // 기본값: 1900-01-01
        }
        
        try {
            return LocalDate.parse(releaseDateStr);
        } catch (DateTimeParseException e) {
            log.error("Invalid date format: {}, using default date", releaseDateStr);
            return LocalDate.of(1900, 1, 1);  // 파싱 실패 시 기본값
        }
    }
}
