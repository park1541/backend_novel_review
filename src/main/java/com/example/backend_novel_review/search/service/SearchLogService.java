package com.example.backend_novel_review.search.service;

import com.example.backend_novel_review.search.dto.SearchLog;
import com.example.backend_novel_review.search.mapper.SearchLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchLogService {

    private final SearchLogMapper searchLogMapper;

    public boolean logSearch(String keyword, String ip) {
        if (searchLogMapper.existsRecent(keyword, ip)) {
            return false;
        }
        searchLogMapper.save(keyword, ip);
        return true;
    }

    @Transactional(readOnly = true)
    public List<SearchLog> getPopularSearches() {
        return searchLogMapper.findPopular();
    }
}
