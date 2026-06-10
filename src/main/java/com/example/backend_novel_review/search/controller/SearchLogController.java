package com.example.backend_novel_review.search.controller;

import com.example.backend_novel_review.search.repository.SearchLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SearchLogController {

    private final SearchLogRepository searchLogRepository;

    // 검색어 기록 (검색 실행 시 프론트가 호출)
    // 같은 IP가 같은 검색어를 1시간 내 재검색하면 기록하지 않음 (순위 조작 방지)
    @PostMapping("/api/search-logs")
    public ResponseEntity<?> logSearch(@RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        String keyword = body.get("keyword");
        if (keyword == null) return ResponseEntity.badRequest().build();

        keyword = keyword.trim();
        if (keyword.isEmpty()) return ResponseEntity.badRequest().build();
        if (keyword.length() > 100) keyword = keyword.substring(0, 100);

        String ip = getClientIp(request);
        if (searchLogRepository.existsRecent(keyword, ip)) {
            return ResponseEntity.ok().build(); // 중복 검색 - 기록 없이 정상 응답
        }

        searchLogRepository.save(keyword, ip);
        return ResponseEntity.status(201).build();
    }

    // 인기 검색어 TOP 5 (최근 7일)
    @GetMapping("/api/search-logs/popular")
    public ResponseEntity<?> getPopularSearches() {
        return ResponseEntity.ok(searchLogRepository.findPopular());
    }

    // 프록시(Railway) 뒤에서도 실제 클라이언트 IP를 얻기 위해 X-Forwarded-For 우선 확인
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim(); // 여러 프록시를 거치면 첫 번째가 원래 클라이언트
        }
        return request.getRemoteAddr();
    }
}
