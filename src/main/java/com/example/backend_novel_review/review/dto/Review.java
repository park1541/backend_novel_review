package com.example.backend_novel_review.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Review {
    private Long id;
    private Long novelId;
    private Long userId;
    private int rating;
    private String content;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String novelTitle;
    private String novelCoverImageUrl;
    private long likeCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
