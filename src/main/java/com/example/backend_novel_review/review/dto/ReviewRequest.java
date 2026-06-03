package com.example.backend_novel_review.review.dto;

import lombok.Getter;

@Getter
public class ReviewRequest {
    private int rating;
    private String content;
}
