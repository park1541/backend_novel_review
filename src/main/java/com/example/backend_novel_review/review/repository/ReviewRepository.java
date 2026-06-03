package com.example.backend_novel_review.review.repository;

import com.example.backend_novel_review.review.domain.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ReviewRepository {
    List<Review> findByNovelId(@Param("novelId") Long novelId,
                               @Param("offset") int offset,
                               @Param("size") int size);

    long countByNovelId(@Param("novelId") Long novelId);

    List<Review> findByUserId(@Param("userId") Long userId,
                              @Param("offset") int offset,
                              @Param("size") int size);

    long countByUserId(@Param("userId") Long userId);

    Optional<Review> findById(@Param("id") Long id);

    void save(@Param("novelId") Long novelId,
              @Param("userId") Long userId,
              @Param("rating") int rating,
              @Param("content") String content);

    void update(@Param("id") Long id,
                @Param("rating") int rating,
                @Param("content") String content);

    void delete(@Param("id") Long id);
}
