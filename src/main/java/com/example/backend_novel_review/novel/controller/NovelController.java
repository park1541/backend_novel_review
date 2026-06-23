package com.example.backend_novel_review.novel.controller;

import com.example.backend_novel_review.novel.dto.NovelRequest;
import com.example.backend_novel_review.novel.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;

    @GetMapping
    public ResponseEntity<?> getNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return ResponseEntity.ok(novelService.getNovels(page, size, genreId, keyword, sortBy));
    }

    @GetMapping("/rankings")
    public ResponseEntity<?> getRankings(
            @RequestParam(defaultValue = "rating") String type,
            @RequestParam(defaultValue = "all") String period,
            @RequestParam(required = false) Long genreId) {
        return ResponseEntity.ok(novelService.getRankings(type, period, genreId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNovel(@PathVariable Long id) {
        return ResponseEntity.ok(novelService.getNovel(id));
    }

    @PostMapping
    public ResponseEntity<?> createNovel(@RequestBody NovelRequest request) {
        novelService.createNovel(request);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNovel(@PathVariable Long id, @RequestBody NovelRequest request) {
        novelService.updateNovel(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNovel(@PathVariable Long id) {
        novelService.deleteNovel(id);
        return ResponseEntity.noContent().build();
    }
}
