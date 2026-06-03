package com.example.backend_novel_review.review.controller;

import com.example.backend_novel_review.review.domain.Review;
import com.example.backend_novel_review.review.dto.ReviewRequest;
import com.example.backend_novel_review.review.repository.ReviewRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;

    // 소설의 리뷰 목록
    @GetMapping("/api/novels/{novelId}/reviews")
    public ResponseEntity<?> getReviews(
            @PathVariable Long novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int offset = page * size;
        List<Review> reviews = reviewRepository.findByNovelId(novelId, offset, size);
        long total = reviewRepository.countByNovelId(novelId);
        long totalPages = (total + size - 1) / size;

        return ResponseEntity.ok(Map.of(
            "content", reviews,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        ));
    }

    // 리뷰 작성
    @PostMapping("/api/novels/{novelId}/reviews")
    public ResponseEntity<?> createReview(@PathVariable Long novelId,
                                          @RequestBody ReviewRequest request) {
        Long userId = getCurrentUserId();
        reviewRepository.save(novelId, userId, request.getRating(), request.getContent());
        return ResponseEntity.status(201).build();
    }

    // 리뷰 수정
    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId,
                                          @RequestBody ReviewRequest request) {
        Long userId = getCurrentUserId();
        Review review = reviewRepository.findById(reviewId)
            .orElse(null);

        if (review == null) return ResponseEntity.notFound().build();

        String role = getCurrentUserRole();
        if (!review.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        reviewRepository.update(reviewId, request.getRating(), request.getContent());
        return ResponseEntity.ok().build();
    }

    // 리뷰 삭제
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        Long userId = getCurrentUserId();
        Review review = reviewRepository.findById(reviewId)
            .orElse(null);

        if (review == null) return ResponseEntity.notFound().build();

        String role = getCurrentUserRole();
        if (!review.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        reviewRepository.delete(reviewId);
        return ResponseEntity.noContent().build();
    }

    // 내 리뷰 목록
    @GetMapping("/api/users/me/reviews")
    public ResponseEntity<?> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        int offset = page * size;
        List<Review> reviews = reviewRepository.findByUserId(userId, offset, size);
        long total = reviewRepository.countByUserId(userId);
        long totalPages = (total + size - 1) / size;

        return ResponseEntity.ok(Map.of(
            "content", reviews,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        ));
    }

    private Long getCurrentUserId() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(claims.getSubject());
    }

    private String getCurrentUserRole() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return claims.get("role", String.class);
    }
}
