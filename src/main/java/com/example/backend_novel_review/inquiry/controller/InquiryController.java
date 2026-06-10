package com.example.backend_novel_review.inquiry.controller;

import com.example.backend_novel_review.inquiry.domain.Inquiry;
import com.example.backend_novel_review.inquiry.repository.InquiryRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryRepository inquiryRepository;

    // 문의 작성
    @PostMapping("/api/inquiries")
    public ResponseEntity<?> createInquiry(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String category = body.get("category");
        String title = body.get("title");
        String content = body.get("content");

        if (isBlank(category) || isBlank(title) || isBlank(content)) {
            return ResponseEntity.badRequest().body(Map.of("message", "유형, 제목, 내용을 모두 입력해주세요."));
        }

        inquiryRepository.save(userId, category.trim(), title.trim(), content.trim());
        return ResponseEntity.status(201).build();
    }

    // 내 문의 목록
    @GetMapping("/api/users/me/inquiries")
    public ResponseEntity<?> getMyInquiries() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(inquiryRepository.findByUserId(userId));
    }

    // 문의 삭제 (본인 + 답변 달리기 전만 가능)
    @DeleteMapping("/api/inquiries/{id}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Inquiry inquiry = inquiryRepository.findById(id).orElse(null);

        if (inquiry == null) return ResponseEntity.notFound().build();
        if (!inquiry.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        if (inquiry.getAnswer() != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "답변이 완료된 문의는 삭제할 수 없습니다."));
        }

        inquiryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // [관리자] 전체 문의 목록
    @GetMapping("/api/admin/inquiries")
    public ResponseEntity<?> getAdminInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int offset = page * size;
        List<Inquiry> inquiries = inquiryRepository.findAll(offset, size);
        long total = inquiryRepository.countAll();
        long totalPages = (total + size - 1) / size;
        return ResponseEntity.ok(Map.of(
            "content", inquiries,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        ));
    }

    // [관리자] 문의 삭제 (답변 여부 상관없이 삭제 가능)
    @DeleteMapping("/api/admin/inquiries/{id}")
    public ResponseEntity<?> deleteAdminInquiry(@PathVariable Long id) {
        if (inquiryRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        inquiryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // [관리자] 답변 등록/수정
    @PutMapping("/api/admin/inquiries/{id}/answer")
    public ResponseEntity<?> answerInquiry(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        String answer = body.get("answer");
        if (isBlank(answer)) {
            return ResponseEntity.badRequest().body(Map.of("message", "답변 내용을 입력해주세요."));
        }
        if (inquiryRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        inquiryRepository.updateAnswer(id, answer.trim());
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
