package com.example.backend_novel_review.user.controller;

import com.example.backend_novel_review.auth.util.CookieUtil;
import com.example.backend_novel_review.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/admin/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @DeleteMapping("/api/users/me")
    public ResponseEntity<?> deleteAccount(Authentication authentication,
                                           HttpServletResponse response) {
        Claims claims = (Claims) authentication.getPrincipal();
        Long userId = Long.valueOf(claims.getSubject());
        userService.deleteAccount(userId);
        CookieUtil.deleteAccessTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<?> banUser(@PathVariable Long id) {
        userService.banUser(id);
        return ResponseEntity.ok().build();
    }
}
