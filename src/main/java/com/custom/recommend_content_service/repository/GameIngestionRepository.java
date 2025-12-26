package com.custom.recommend_content_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.custom.recommend_content_service.entity.Game;

@Repository
public interface GameIngestionRepository extends JpaRepository<Game, Long> {

   /**
     * 제목으로 게임 검색
     */
    Game findByName(String name);
    
    /**
     * 인기도 기준으로 상위 N개 조회
     */
    List<Game> findAllByOrderByClickCntDesc();
}
