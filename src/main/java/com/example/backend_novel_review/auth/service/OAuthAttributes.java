package com.example.backend_novel_review.auth.service;

import com.example.backend_novel_review.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {

    private String provider;
    private String providerId;
    private String email;
    private String nickname;
    private String profileImageUrl;

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "naver" -> ofNaver(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> ofGoogle(attributes);
        };
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
            .provider("GOOGLE")
            .providerId((String) attributes.get("sub"))
            .email((String) attributes.get("email"))
            .nickname((String) attributes.get("name"))
            .profileImageUrl((String) attributes.get("picture"))
            .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        String providerId = (String) response.get("id");
        String email = (String) response.get("email");
        if (email == null) email = "naver_" + providerId + "@naver.com";
        return OAuthAttributes.builder()
            .provider("NAVER")
            .providerId(providerId)
            .email(email)
            .nickname((String) response.get("nickname"))
            .profileImageUrl((String) response.get("profile_image"))
            .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String providerId = String.valueOf(attributes.get("id"));
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        if (email == null) email = "kakao_" + providerId + "@kakao.com";
        return OAuthAttributes.builder()
            .provider("KAKAO")
            .providerId(providerId)
            .email(email)
            .nickname(properties != null ? (String) properties.get("nickname") : "카카오유저")
            .profileImageUrl(properties != null ? (String) properties.get("profile_image") : null)
            .build();
    }

    public User toEntity(String socialAccessToken) {
        return User.builder()
            .email(email)
            .nickname(nickname)
            .profileImageUrl(profileImageUrl)
            .role("USER")
            .provider(provider)
            .providerId(providerId)
            .socialAccessToken(socialAccessToken)
            .build();
    }
}
