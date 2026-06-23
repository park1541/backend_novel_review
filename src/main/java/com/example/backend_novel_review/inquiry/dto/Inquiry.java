package com.example.backend_novel_review.inquiry.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Inquiry {
    private Long id;
    private Long userId;
    private String userNickname;
    private String category;
    private String title;
    private String content;
    private String answer;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;
}
