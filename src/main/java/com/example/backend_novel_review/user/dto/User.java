package com.example.backend_novel_review.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String provider;
    private String providerId;
    private String socialAccessToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User update(String nickname, String profileImageUrl, String socialAccessToken) {
        return User.builder()
            .id(this.id)
            .email(this.email)
            .nickname(nickname)
            .profileImageUrl(profileImageUrl)
            .role(this.role)
            .provider(this.provider)
            .providerId(this.providerId)
            .socialAccessToken(socialAccessToken)
            .createdAt(this.createdAt)
            .build();
    }
}
