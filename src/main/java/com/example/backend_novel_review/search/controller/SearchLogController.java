package com.example.backend_novel_review.search.controller;

import com.example.backend_novel_review.search.service.SearchLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SearchLogController {

    private final SearchLogService searchLogService;

    @PostMapping("/api/search-logs")
    public ResponseEntity<?> logSearch(@RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        String keyword = body.get("keyword");
        if (keyword == null) return ResponseEntity.badRequest().build();
        keyword = keyword.trim();
        if (keyword.isEmpty()) return ResponseEntity.badRequest().build();
        if (keyword.length() > 100) keyword = keyword.substring(0, 100);

        boolean saved = searchLogService.logSearch(keyword, getClientIp(request));
        return saved ? ResponseEntity.status(201).build() : ResponseEntity.ok().build();
    }

    @GetMapping("/api/search-logs/popular")
    public ResponseEntity<?> getPopularSearches() {
        return ResponseEntity.ok(searchLogService.getPopularSearches());
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
