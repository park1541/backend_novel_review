package com.example.backend_novel_review.genre.controller;

import com.example.backend_novel_review.genre.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping("/api/genres")
    public ResponseEntity<?> getGenres() {
        return ResponseEntity.ok(genreService.getGenres());
    }

    @PostMapping("/api/admin/genres")
    public ResponseEntity<?> createGenre(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "장르 이름을 입력해주세요."));
        }
        genreService.createGenre(name.trim());
        return ResponseEntity.status(201).body(Map.of("message", "장르가 추가됐습니다."));
    }

    @PutMapping("/api/admin/genres/{id}")
    public ResponseEntity<?> updateGenre(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "장르 이름을 입력해주세요."));
        }
        genreService.updateGenre(id, name.trim());
        return ResponseEntity.ok(Map.of("message", "장르가 수정됐습니다."));
    }

    @DeleteMapping("/api/admin/genres/{id}")
    public ResponseEntity<?> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}
