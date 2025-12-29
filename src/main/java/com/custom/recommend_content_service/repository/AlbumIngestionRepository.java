package com.custom.recommend_content_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.custom.recommend_content_service.entity.Album;

@Repository
public interface AlbumIngestionRepository extends JpaRepository<Album, Long> {
    
    Optional<Album> findByAlbumId(String albumId);
    
    boolean existsByAlbumId(String albumId);
    
    @Query("SELECT a FROM Album a ORDER BY a.clickCnt DESC, a.popularity DESC")
    List<Album> findAllOrderByClickCntAndPopularity();
    
    @Query("SELECT a FROM Album a ORDER BY a.createdAt DESC")
    List<Album> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT a FROM Album a ORDER BY a.popularity DESC")
    List<Album> findAllOrderByPopularityDesc();
    
    List<Album> findTop20ByOrderByClickCntDesc();
    
    List<Album> findTop20ByOrderByPopularityDesc();
    
    List<Album> findTop20ByOrderByCreatedAtDesc();
}