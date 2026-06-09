package com.example.backend_novel_review.review.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewReport {
    private Long id;
    private Long reviewId;
    private Long reporterId;
    private String reporterNickname;
    private String reviewContent;
    private String reason;
    private LocalDateTime createdAt;
}
