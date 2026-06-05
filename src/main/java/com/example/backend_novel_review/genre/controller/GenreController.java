package com.example.backend_novel_review.genre.controller;

import com.example.backend_novel_review.genre.domain.Genre;
import com.example.backend_novel_review.genre.repository.GenreRepository;
import com.example.backend_novel_review.novel.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;
    private final NovelRepository novelRepository;

    // 공개 - 장르 목록 조회
    @GetMapping("/api/genres")
    public ResponseEntity<List<Genre>> getGenres() {
        return ResponseEntity.ok(genreRepository.findAll());
    }

    // ADMIN - 장르 추가
    @PostMapping("/api/admin/genres")
    public ResponseEntity<?> createGenre(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "장르 이름을 입력해주세요."));
        }
        genreRepository.save(name.trim());
        return ResponseEntity.status(201).body(Map.of("message", "장르가 추가됐습니다."));
    }

    // ADMIN - 장르 수정
    @PutMapping("/api/admin/genres/{id}")
    public ResponseEntity<?> updateGenre(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "장르 이름을 입력해주세요."));
        }
        genreRepository.update(id, name.trim());
        return ResponseEntity.ok(Map.of("message", "장르가 수정됐습니다."));
    }

    // ADMIN - 장르 삭제
    @DeleteMapping("/api/admin/genres/{id}")
    public ResponseEntity<?> deleteGenre(@PathVariable Long id) {
        long novelCount = novelRepository.countByGenreId(id);
        if (novelCount > 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "해당 장르로 등록된 소설이 있습니다."));
        }
        genreRepository.delete(id);
        return ResponseEntity.noContent().build();
    }
}
