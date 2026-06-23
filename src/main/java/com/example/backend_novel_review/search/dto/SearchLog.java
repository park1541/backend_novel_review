package com.example.backend_novel_review.search.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchLog {
    private String keyword;
    private long count;
}
