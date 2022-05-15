package com.example.model.jpa.repository;

import com.example.model.jpa.MusicGenre;
import com.example.model.jpa.YoutubeDataInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface YoutubeDataRepository extends JpaRepository<YoutubeDataInfo, Long>, JpaSpecificationExecutor<YoutubeDataInfo> {

    Page<YoutubeDataInfo> findAllByDeleted(boolean isDeleted, Pageable request);
    Optional<YoutubeDataInfo> findByTitleOrUrlId(String title, String urlId);
    List<YoutubeDataInfo> findAllByGenre(MusicGenre genre);
    YoutubeDataInfo save(YoutubeDataInfo member);
}
