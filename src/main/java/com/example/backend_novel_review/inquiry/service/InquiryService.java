package com.example.backend_novel_review.inquiry.service;

import com.example.backend_novel_review.inquiry.dto.Inquiry;
import com.example.backend_novel_review.inquiry.mapper.InquiryMapper;
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
public class InquiryService {

    private final InquiryMapper inquiryMapper;

    public void createInquiry(Long userId, String category, String title, String content) {
        inquiryMapper.save(userId, category, title, content);
    }

    @Transactional(readOnly = true)
    public List<Inquiry> getMyInquiries(Long userId) {
        return inquiryMapper.findByUserId(userId);
    }

    public void deleteInquiry(Long id, Long userId) {
        Inquiry inquiry = inquiryMapper.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!inquiry.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (inquiry.getAnswer() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변이 완료된 문의는 삭제할 수 없습니다.");
        }
        inquiryMapper.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminInquiries(int page, int size) {
        int offset = page * size;
        List<Inquiry> inquiries = inquiryMapper.findAll(offset, size);
        long total = inquiryMapper.countAll();
        long totalPages = (total + size - 1) / size;
        return Map.of(
            "content", inquiries,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        );
    }

    public void deleteAdminInquiry(Long id) {
        inquiryMapper.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inquiryMapper.deleteById(id);
    }

    public void answerInquiry(Long id, String answer) {
        inquiryMapper.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inquiryMapper.updateAnswer(id, answer);
    }
}
