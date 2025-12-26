package com.custom.recommend_content_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.custom.recommend_content_service.entity.Music;

@Repository
public interface SpotifyIngestionRepository extends JpaRepository<Music, Long>{
    
}
