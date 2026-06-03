package com.example.backend_novel_review.genre.controller;

import com.example.backend_novel_review.genre.domain.Genre;
import com.example.backend_novel_review.genre.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;

    @GetMapping
    public ResponseEntity<List<Genre>> getGenres() {
        return ResponseEntity.ok(genreRepository.findAll());
    }
}
