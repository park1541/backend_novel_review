package com.example.backend_novel_review.search.repository;

import com.example.backend_novel_review.search.domain.SearchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchLogRepository {
    void save(@Param("keyword") String keyword, @Param("ipAddress") String ipAddress);
    boolean existsRecent(@Param("keyword") String keyword, @Param("ipAddress") String ipAddress);
    List<SearchLog> findPopular();
}
