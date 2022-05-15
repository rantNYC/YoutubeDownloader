package com.example.model.jpa.repository;

import com.example.model.jpa.MusicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MusicGenreRepository extends JpaRepository<MusicGenre, Long>, JpaSpecificationExecutor<MusicGenre> {
    Optional<MusicGenre> findByName(String name);
}
