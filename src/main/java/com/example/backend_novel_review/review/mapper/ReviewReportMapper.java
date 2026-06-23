package com.example.backend_novel_review.review.mapper;

import com.example.backend_novel_review.review.dto.ReviewReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewReportMapper {
    void save(@Param("reviewId") Long reviewId, @Param("reporterId") Long reporterId, @Param("reason") String reason);
    List<ReviewReport> findAll(@Param("offset") int offset, @Param("size") int size);
    long countAll();
    boolean existsByReviewIdAndReporterId(@Param("reviewId") Long reviewId, @Param("reporterId") Long reporterId);
    void deleteById(@Param("id") Long id);
}
