package com.example.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YoutubeDataRepository extends JpaRepository<YoutubeDataInfo, Long> {
    Optional<YoutubeDataInfo> findByTitle(String title);
    YoutubeDataInfo save(YoutubeDataInfo member);
}
