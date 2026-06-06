package com.example.backend_novel_review.review.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewLikeRepository {
    void save(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    void delete(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    boolean exists(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    long countByReviewId(@Param("reviewId") Long reviewId);
}
