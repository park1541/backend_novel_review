package com.example.backend_novel_review.user.controller;

import com.example.backend_novel_review.auth.service.SocialUnlinkService;
import com.example.backend_novel_review.auth.util.CookieUtil;
import com.example.backend_novel_review.review.repository.ReviewRepository;
import com.example.backend_novel_review.user.domain.User;
import com.example.backend_novel_review.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final SocialUnlinkService socialUnlinkService;

    @GetMapping("/api/admin/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/api/users/me")
    public ResponseEntity<?> deleteAccount(Authentication authentication,
                                           HttpServletResponse response) {
        Claims claims = (Claims) authentication.getPrincipal();
        Long userId = Long.valueOf(claims.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 소셜 연동 해제 (실패해도 탈퇴 진행)
        socialUnlinkService.unlink(user.getProvider(), user.getProviderId(), user.getSocialAccessToken());

        // DB에서 완전 삭제 (FK ON DELETE SET NULL으로 reviews.user_id 자동 NULL)
        userRepository.deleteById(user.getId());

        // JWT 쿠키 삭제
        CookieUtil.deleteAccessTokenCookie(response);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<?> banUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 리뷰 전체 삭제
        reviewRepository.deleteByUserId(user.getId());

        // 소셜 연동 해제 (실패해도 진행)
        socialUnlinkService.unlink(user.getProvider(), user.getProviderId(), user.getSocialAccessToken());

        // DB에서 완전 삭제
        userRepository.deleteById(user.getId());

        return ResponseEntity.ok().build();
    }
}
