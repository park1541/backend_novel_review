package com.example.backend_novel_review.inquiry.repository;

import com.example.backend_novel_review.inquiry.domain.Inquiry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface InquiryRepository {
    void save(@Param("userId") Long userId, @Param("category") String category,
              @Param("title") String title, @Param("content") String content);
    List<Inquiry> findByUserId(@Param("userId") Long userId);
    Optional<Inquiry> findById(@Param("id") Long id);
    List<Inquiry> findAll(@Param("offset") int offset, @Param("size") int size);
    long countAll();
    void updateAnswer(@Param("id") Long id, @Param("answer") String answer);
    void deleteById(@Param("id") Long id);
}
