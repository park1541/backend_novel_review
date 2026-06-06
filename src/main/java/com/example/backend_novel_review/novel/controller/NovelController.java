package com.example.backend_novel_review.novel.controller;

import com.example.backend_novel_review.novel.domain.Novel;
import com.example.backend_novel_review.novel.dto.NovelRequest;
import com.example.backend_novel_review.novel.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelRepository novelRepository;

    @GetMapping
    public ResponseEntity<?> getNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy) {

        int offset = page * size;
        List<Novel> novels = novelRepository.findAll(genreId, keyword, sortBy, offset, size);
        long total = novelRepository.count(genreId, keyword);
        long totalPages = (total + size - 1) / size;

        return ResponseEntity.ok(Map.of(
            "content", novels,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNovel(@PathVariable Long id) {
        return novelRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createNovel(@RequestBody NovelRequest request) {
        novelRepository.save(request);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNovel(@PathVariable Long id, @RequestBody NovelRequest request) {
        novelRepository.update(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNovel(@PathVariable Long id) {
        novelRepository.delete(id);
        return ResponseEntity.noContent().build();
    }
}
