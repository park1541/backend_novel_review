package com.example.backend_novel_review.novel.service;

import com.example.backend_novel_review.novel.dto.Novel;
import com.example.backend_novel_review.novel.dto.NovelRequest;
import com.example.backend_novel_review.novel.mapper.NovelMapper;
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
public class NovelService {

    private final NovelMapper novelMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> getNovels(int page, int size, Long genreId, String keyword, String sortBy) {
        int offset = page * size;
        List<Novel> novels = novelMapper.findAll(genreId, keyword, sortBy, offset, size);
        long total = novelMapper.count(genreId, keyword);
        long totalPages = (total + size - 1) / size;
        return Map.of(
            "content", novels,
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages
        );
    }

    @Transactional(readOnly = true)
    public List<Novel> getRankings(String type, String period, Long genreId) {
        return novelMapper.findRankings(type, period, genreId);
    }

    @Transactional(readOnly = true)
    public Novel getNovel(Long id) {
        return novelMapper.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "소설을 찾을 수 없습니다."));
    }

    public void createNovel(NovelRequest request) {
        novelMapper.save(request);
    }

    public void updateNovel(Long id, NovelRequest request) {
        novelMapper.update(id, request);
    }

    public void deleteNovel(Long id) {
        novelMapper.delete(id);
    }
}
