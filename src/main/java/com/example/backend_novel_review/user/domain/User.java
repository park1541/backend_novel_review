package com.example.backend_novel_review.user.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class User {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String role;          // USER, ADMIN
    private String provider;      // GOOGLE, NAVER, KAKAO
    private String providerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User update(String nickname, String profileImageUrl) {
        return User.builder()
            .id(this.id)
            .email(this.email)
            .nickname(nickname)
            .profileImageUrl(profileImageUrl)
            .role(this.role)
            .provider(this.provider)
            .providerId(this.providerId)
            .createdAt(this.createdAt)
            .build();
    }
}
