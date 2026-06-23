package com.example.backend_novel_review.novel.mapper;

import com.example.backend_novel_review.novel.dto.Novel;
import com.example.backend_novel_review.novel.dto.NovelRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NovelMapper {
    List<Novel> findAll(@Param("genreId") Long genreId,
                        @Param("keyword") String keyword,
                        @Param("sortBy") String sortBy,
                        @Param("offset") int offset,
                        @Param("size") int size);

    long count(@Param("genreId") Long genreId, @Param("keyword") String keyword);

    List<Novel> findRankings(@Param("type") String type,
                             @Param("period") String period,
                             @Param("genreId") Long genreId);

    Optional<Novel> findById(@Param("id") Long id);

    void save(NovelRequest request);

    void update(@Param("id") Long id, @Param("req") NovelRequest request);

    void delete(@Param("id") Long id);

    long countByGenreId(@Param("genreId") Long genreId);
}
