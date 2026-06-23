package com.example.backend_novel_review.review.controller;

import com.example.backend_novel_review.review.dto.ReviewRequest;
import com.example.backend_novel_review.review.service.ReviewService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 소설의 리뷰 목록
    @GetMapping("/api/novels/{novelId}/reviews")
    public ResponseEntity<?> getReviews(
            @PathVariable Long novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviews(novelId, page, size, getOptionalUserId()));
    }

    // 리뷰 좋아요 토글
    @PostMapping("/api/reviews/{reviewId}/likes")
    public ResponseEntity<?> toggleLike(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.toggleLike(reviewId, getCurrentUserId()));
    }

    // 리뷰 작성
    @PostMapping("/api/novels/{novelId}/reviews")
    public ResponseEntity<?> createReview(@PathVariable Long novelId,
                                          @RequestBody ReviewRequest request) {
        reviewService.createReview(novelId, getCurrentUserId(), request);
        return ResponseEntity.status(201).build();
    }

    // 리뷰 수정
    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId,
                                          @RequestBody ReviewRequest request) {
        reviewService.updateReview(reviewId, getCurrentUserId(), getCurrentUserRole(), request);
        return ResponseEntity.ok().build();
    }

    // 리뷰 삭제
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId, getCurrentUserId(), getCurrentUserRole());
        return ResponseEntity.noContent().build();
    }

    // 내 리뷰 목록
    @GetMapping("/api/users/me/reviews")
    public ResponseEntity<?> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getMyReviews(getCurrentUserId(), page, size));
    }

    // 리뷰 신고 등록
    @PostMapping("/api/reviews/{reviewId}/reports")
    public ResponseEntity<?> reportReview(@PathVariable Long reviewId,
                                          @RequestBody Map<String, String> body) {
        reviewService.reportReview(reviewId, getCurrentUserId(), body.get("reason"));
        return ResponseEntity.status(201).build();
    }

    // [관리자] 신고 목록 조회
    @GetMapping("/api/admin/review-reports")
    public ResponseEntity<?> getReviewReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getReviewReports(page, size));
    }

    // [관리자] 신고 거절 (신고만 삭제)
    @DeleteMapping("/api/admin/review-reports/{id}")
    public ResponseEntity<?> dismissReport(@PathVariable Long id) {
        reviewService.dismissReport(id);
        return ResponseEntity.noContent().build();
    }

    // [관리자] 신고 처리 (리뷰 삭제 → review_reports도 CASCADE로 자동 삭제)
    @DeleteMapping("/api/admin/review-reports/{id}/delete-review/{reviewId}")
    public ResponseEntity<?> deleteReviewByReport(@PathVariable Long id,
                                                   @PathVariable Long reviewId) {
        reviewService.deleteReviewByReport(reviewId);
        return ResponseEntity.noContent().build();
    }

    // --- SecurityContext에서 인증 정보 추출 (web 계층 관심사) ---

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
