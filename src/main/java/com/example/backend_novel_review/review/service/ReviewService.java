package com.example.backend_novel_review.review.service;

import com.example.backend_novel_review.review.dto.Review;
import com.example.backend_novel_review.review.dto.ReviewReport;
import com.example.backend_novel_review.review.dto.ReviewRequest;
import com.example.backend_novel_review.review.mapper.ReviewLikeMapper;
import com.example.backend_novel_review.review.mapper.ReviewMapper;
import com.example.backend_novel_review.review.mapper.ReviewReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ReviewLikeMapper reviewLikeMapper;
    private final ReviewReportMapper reviewReportMapper;

    // 소설의 리뷰 목록 (로그인 시 liked 여부 세팅)
    @Transactional(readOnly = true)
    public Map<String, Object> getReviews(Long novelId, int page, int size, Long currentUserId) {
        int offset = page * size;
        List<Review> reviews = reviewMapper.findByNovelId(novelId, offset, size);
        long total = reviewMapper.countByNovelId(novelId);

        if (currentUserId != null) {
            reviews.forEach(r -> r.setLiked(reviewLikeMapper.exists(r.getId(), currentUserId)));
        }
        return pageResponse(reviews, page, size, total);
    }

    // 리뷰 좋아요 토글
    public Map<String, Object> toggleLike(Long reviewId, Long userId) {
        boolean alreadyLiked = reviewLikeMapper.exists(reviewId, userId);
        if (alreadyLiked) {
            reviewLikeMapper.delete(reviewId, userId);
        } else {
            reviewLikeMapper.save(reviewId, userId);
        }
        long likeCount = reviewLikeMapper.countByReviewId(reviewId);
        return Map.of("liked", !alreadyLiked, "likeCount", likeCount);
    }

    // 리뷰 작성
    public void createReview(Long novelId, Long userId, ReviewRequest request) {
        reviewMapper.save(novelId, userId, request.getRating(), request.getContent());
    }

    // 리뷰 수정 (본인 또는 ADMIN)
    public void updateReview(Long reviewId, Long userId, String role, ReviewRequest request) {
        Review review = findOrThrow(reviewId);
        requireOwnerOrAdmin(review, userId, role);
        reviewMapper.update(reviewId, request.getRating(), request.getContent());
    }

    // 리뷰 삭제 (본인 또는 ADMIN)
    public void deleteReview(Long reviewId, Long userId, String role) {
        Review review = findOrThrow(reviewId);
        requireOwnerOrAdmin(review, userId, role);
        reviewMapper.delete(reviewId);
    }

    // 내 리뷰 목록
    @Transactional(readOnly = true)
    public Map<String, Object> getMyReviews(Long userId, int page, int size) {
        int offset = page * size;
        List<Review> reviews = reviewMapper.findByUserId(userId, offset, size);
        long total = reviewMapper.countByUserId(userId);
        return pageResponse(reviews, page, size, total);
    }

    // 리뷰 신고 등록 (빈 사유 400, 중복 409)
    public void reportReview(Long reviewId, Long reporterId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "신고 이유를 입력해주세요.");
        }
        if (reviewReportMapper.existsByReviewIdAndReporterId(reviewId, reporterId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신고한 리뷰입니다.");
        }
        reviewReportMapper.save(reviewId, reporterId, reason);
    }

    // [관리자] 신고 목록
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewReports(int page, int size) {
        int offset = page * size;
        List<ReviewReport> reports = reviewReportMapper.findAll(offset, size);
        long total = reviewReportMapper.countAll();
        return pageResponse(reports, page, size, total);
    }

    // [관리자] 신고 거절 (신고만 삭제)
    public void dismissReport(Long id) {
        reviewReportMapper.deleteById(id);
    }

    // [관리자] 신고 처리 (리뷰 삭제 → review_reports CASCADE 삭제)
    public void deleteReviewByReport(Long reviewId) {
        reviewMapper.delete(reviewId);
    }

    // --- 내부 헬퍼 ---

    private Review findOrThrow(Long reviewId) {
        return reviewMapper.findById(reviewId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
    }

    private void requireOwnerOrAdmin(Review review, Long userId, String role) {
        if (!review.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
    }

    private Map<String, Object> pageResponse(Object content, int page, int size, long total) {
        long totalPages = (total + size - 1) / size;
        return Map.of(
            "content", content,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        );
    }
}
