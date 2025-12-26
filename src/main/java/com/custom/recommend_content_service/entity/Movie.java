package com.custom.recommend_content_service.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class) 
@Table(name = "movie")
public class Movie {

    // TMDB 영화 ID (고유 식별자)
    @Id
    @Column(name = "movie_id")
    private Long id;
    
    // 제목
    @Column(nullable = false, length = 500, name="title")
    private String title;

    // 영화 개요/설명
    @Column(nullable = true, columnDefinition = "TEXT", name="overview")
    private String overview;

    // 장르 ID 목록
    @ElementCollection
    @Builder.Default
    private List<Integer> genreIds = new ArrayList<>();

    // 평균 평점
    @Column(nullable = true, name="voteAverage")
    private Double voteAverage;
    
    // 인기도
    @Column(nullable = true, name="popularity")
    private Double popularity;
    
    // 개봉일
    @Column(nullable = true, name="release_date")
    private LocalDate releaseDate;
    
    // 포스터 이미지 경로
    @Column(nullable = true, length = 500, name="poster_path")
    private String posterPath;

    @Column(name = "clickCnt")
    Integer clickCnt;

    /**
     * Movie 엔티티 생성 정적 팩토리 메서드
     */
    public static Movie create(
        Long id,
        String title,
        String overview,
        List<Integer> genreIds,
        Double voteAverage,
        Double popularity,
        LocalDate releaseDate,
        String posterPath,
        Integer clickCnt
    ) {
        
        return Movie.builder()
            .id(id)
            .title(title)
            .overview(overview)
            .genreIds(genreIds != null ? new ArrayList<>(genreIds) : new ArrayList<>())
            .voteAverage(voteAverage)
            .popularity(popularity)
            .releaseDate(releaseDate)
            .posterPath(posterPath)
            .clickCnt(clickCnt)
            .build();
    }

    /**
     * Movie 엔티티 업데이트 메서드
     */
    public void update(
            String title,
            String overview,
            List<Integer> genreIds,
            Double voteAverage,
            Double popularity,
            LocalDate releaseDate,
            String posterPath) {
        
        this.title = title;
        this.overview = overview;
        this.genreIds = genreIds != null ? new ArrayList<>(genreIds) : new ArrayList<>();
        this.voteAverage = voteAverage;
        this.popularity = popularity;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
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