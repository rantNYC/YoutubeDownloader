package com.example.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface YoutubeDataRepository extends JpaRepository<YoutubeDataInfo, Long>, JpaSpecificationExecutor<YoutubeDataInfo> {

    Optional<YoutubeDataInfo> findByTitle(String title);
    YoutubeDataInfo save(YoutubeDataInfo member);

    @Modifying
    @Query("update YoutubeDataInfo y set y.title = ?1 where y.id = ?2")
    void setTitleById(@NonNull String title, @NonNull Long id);
}
