package com.custom.recommend_content_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.custom.recommend_content_service.entity.Movie;

@Repository
public interface MovieIngestionRepository extends JpaRepository<Movie, Long> {

   /**
     * 제목으로 영화 검색
     */
    Optional<Movie> findByTitle(String title);
    
    /**
     * 인기도 기준으로 상위 N개 조회
     */
    List<Movie> findTop20ByOrderByPopularityDesc();
    
    /**
     * 평점 기준으로 상위 N개 조회
     */
    List<Movie> findTop20ByOrderByVoteAverageDesc();
    
    /**
     * 특정 장르 ID를 포함하는 영화 검색
     */
    @Query("SELECT m FROM Movie m WHERE :genreId MEMBER OF m.genreIds")
    List<Movie> findByGenreId(@Param("genreId") Integer genreId);
}
