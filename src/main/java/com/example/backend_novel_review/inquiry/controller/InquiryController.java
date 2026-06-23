package com.example.backend_novel_review.inquiry.controller;

import com.example.backend_novel_review.inquiry.service.InquiryService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping("/api/inquiries")
    public ResponseEntity<?> createInquiry(@RequestBody Map<String, String> body) {
        String category = body.get("category");
        String title = body.get("title");
        String content = body.get("content");
        if (isBlank(category) || isBlank(title) || isBlank(content)) {
            return ResponseEntity.badRequest().body(Map.of("message", "유형, 제목, 내용을 모두 입력해주세요."));
        }
        inquiryService.createInquiry(getCurrentUserId(), category.trim(), title.trim(), content.trim());
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/api/users/me/inquiries")
    public ResponseEntity<?> getMyInquiries() {
        return ResponseEntity.ok(inquiryService.getMyInquiries(getCurrentUserId()));
    }

    @DeleteMapping("/api/inquiries/{id}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/inquiries")
    public ResponseEntity<?> getAdminInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inquiryService.getAdminInquiries(page, size));
    }

    @DeleteMapping("/api/admin/inquiries/{id}")
    public ResponseEntity<?> deleteAdminInquiry(@PathVariable Long id) {
        inquiryService.deleteAdminInquiry(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/admin/inquiries/{id}/answer")
    public ResponseEntity<?> answerInquiry(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        String answer = body.get("answer");
        if (isBlank(answer)) {
            return ResponseEntity.badRequest().body(Map.of("message", "답변 내용을 입력해주세요."));
        }
        inquiryService.answerInquiry(id, answer.trim());
        return ResponseEntity.ok().build();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private Long getCurrentUserId() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(claims.getSubject());
    }
}
