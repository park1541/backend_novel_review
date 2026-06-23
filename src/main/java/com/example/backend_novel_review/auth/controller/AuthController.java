package com.example.backend_novel_review.auth.controller;

import com.example.backend_novel_review.auth.util.CookieUtil;
import com.example.backend_novel_review.user.dto.User;
import com.example.backend_novel_review.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        Optional<String> token = CookieUtil.getAccessToken(request);
        if (token.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Claims claims = (Claims) authentication.getPrincipal();
        Long userId = Long.parseLong(claims.getSubject());

        return userMapper.findById(userId)
            .map(user -> ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "nickname", user.getNickname(),
                "profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "",
                "role", user.getRole()
            )))
            .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        CookieUtil.deleteAccessTokenCookie(response);
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
}
