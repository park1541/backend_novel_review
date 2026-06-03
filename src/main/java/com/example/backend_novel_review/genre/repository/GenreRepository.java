package com.example.backend_novel_review.genre.repository;

import com.example.backend_novel_review.genre.domain.Genre;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GenreRepository {
    List<Genre> findAll();
}
