package com.example.backend_novel_review.genre.repository;

import com.example.backend_novel_review.genre.domain.Genre;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GenreRepository {
    List<Genre> findAll();
    void save(@Param("name") String name);
    void update(@Param("id") Long id, @Param("name") String name);
    void delete(@Param("id") Long id);
}
