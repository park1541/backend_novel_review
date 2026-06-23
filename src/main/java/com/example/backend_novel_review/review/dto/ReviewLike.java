package com.example.backend_novel_review.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewLike {
    private Long id;
    private Long reviewId;
    private Long userId;
    private LocalDateTime createdAt;
}
