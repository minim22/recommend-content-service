package com.custom.recommend_content_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.custom.recommend_content_service.entity.Track;

@Repository
public interface TrackIngestionRepository extends JpaRepository<Track, Long> {
    
    Optional<Track> findByTrackId(String trackId);
    
    boolean existsByTrackId(String trackId);
    
    List<Track> findByAlbumId(Long albumId);
    
    @Query("SELECT t FROM Track t WHERE t.album.albumId = :albumId ORDER BY t.trackNumber ASC")
    List<Track> findByAlbumAlbumIdOrderByTrackNumber(@Param("albumId") String albumId);
    
    void deleteByAlbumId(Long albumId);
}