package com.example.backend_novel_review.novel.dto;

import lombok.Getter;

@Getter
public class NovelRequest {
    private String title;
    private String author;
    private String description;
    private String coverImageUrl;
    private Long genreId;
}
