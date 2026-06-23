package com.example.backend_novel_review.novel.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Novel {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String coverImageUrl;
    private Long genreId;
    private String genreName;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
}
