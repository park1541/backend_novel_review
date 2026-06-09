package com.example.backend_novel_review.review.controller;

import com.example.backend_novel_review.review.domain.Review;
import com.example.backend_novel_review.review.domain.ReviewReport;
import com.example.backend_novel_review.review.dto.ReviewRequest;
import com.example.backend_novel_review.review.repository.ReviewLikeRepository;
import com.example.backend_novel_review.review.repository.ReviewReportRepository;
import com.example.backend_novel_review.review.repository.ReviewRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewReportRepository reviewReportRepository;

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

        // 로그인한 경우 liked 여부 세팅
        Long userId = getOptionalUserId();
        if (userId != null) {
            reviews.forEach(r -> r.setLiked(reviewLikeRepository.exists(r.getId(), userId)));
        }

        return ResponseEntity.ok(Map.of(
            "content", reviews,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        ));
    }

    // 리뷰 좋아요 토글
    @PostMapping("/api/reviews/{reviewId}/likes")
    public ResponseEntity<?> toggleLike(@PathVariable Long reviewId) {
        Long userId = getCurrentUserId();
        boolean alreadyLiked = reviewLikeRepository.exists(reviewId, userId);
        if (alreadyLiked) {
            reviewLikeRepository.delete(reviewId, userId);
        } else {
            reviewLikeRepository.save(reviewId, userId);
        }
        long likeCount = reviewLikeRepository.countByReviewId(reviewId);
        return ResponseEntity.ok(Map.of("liked", !alreadyLiked, "likeCount", likeCount));
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

    // 리뷰 신고 등록
    @PostMapping("/api/reviews/{reviewId}/reports")
    public ResponseEntity<?> reportReview(@PathVariable Long reviewId,
                                          @RequestBody Map<String, String> body) {
        Long reporterId = getCurrentUserId();
        String reason = body.get("reason");
        if (reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "신고 이유를 입력해주세요."));
        }
        if (reviewReportRepository.existsByReviewIdAndReporterId(reviewId, reporterId)) {
            return ResponseEntity.status(409).body(Map.of("message", "이미 신고한 리뷰입니다."));
        }
        reviewReportRepository.save(reviewId, reporterId, reason);
        return ResponseEntity.status(201).build();
    }

    // [관리자] 신고 목록 조회
    @GetMapping("/api/admin/review-reports")
    public ResponseEntity<?> getReviewReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int offset = page * size;
        List<ReviewReport> reports = reviewReportRepository.findAll(offset, size);
        long total = reviewReportRepository.countAll();
        long totalPages = (total + size - 1) / size;
        return ResponseEntity.ok(Map.of(
            "content", reports,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        ));
    }

    // [관리자] 신고 거절 (신고만 삭제)
    @DeleteMapping("/api/admin/review-reports/{id}")
    public ResponseEntity<?> dismissReport(@PathVariable Long id) {
        reviewReportRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // [관리자] 신고 처리 (리뷰 삭제 → review_reports도 CASCADE로 자동 삭제)
    @DeleteMapping("/api/admin/review-reports/{id}/delete-review/{reviewId}")
    public ResponseEntity<?> deleteReviewByReport(@PathVariable Long id,
                                                   @PathVariable Long reviewId) {
        reviewRepository.delete(reviewId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(claims.getSubject());
    }

    private Long getOptionalUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (!(principal instanceof Claims)) return null;
            return Long.parseLong(((Claims) principal).getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentUserRole() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return claims.get("role", String.class);
    }
}
