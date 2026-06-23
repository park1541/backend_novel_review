package com.example.backend_novel_review.genre.service;

import com.example.backend_novel_review.genre.dto.Genre;
import com.example.backend_novel_review.genre.mapper.GenreMapper;
import com.example.backend_novel_review.novel.mapper.NovelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GenreService {

    private final GenreMapper genreMapper;
    private final NovelMapper novelMapper;

    @Transactional(readOnly = true)
    public List<Genre> getGenres() {
        return genreMapper.findAll();
    }

    public void createGenre(String name) {
        genreMapper.save(name);
    }

    public void updateGenre(Long id, String name) {
        genreMapper.update(id, name);
    }

    public void deleteGenre(Long id) {
        if (novelMapper.countByGenreId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 장르로 등록된 소설이 있습니다.");
        }
        genreMapper.delete(id);
    }
}
