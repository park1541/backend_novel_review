package com.example.backend_novel_review.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SocialUnlinkService {

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public void unlink(String provider, String providerId, String accessToken) {
        try {
            switch (provider) {
                case "KAKAO" -> unlinkKakao(providerId);
                case "GOOGLE" -> unlinkGoogle(accessToken);
                case "NAVER" -> unlinkNaver(accessToken);
                default -> log.warn("알 수 없는 provider: {}", provider);
            }
        } catch (Exception e) {
            // 연동 해제 실패해도 탈퇴는 계속 진행
            log.warn("소셜 연동 해제 실패 (provider={}, 탈퇴는 계속 진행): {}", provider, e.getMessage());
        }
    }

    private void unlinkKakao(String providerId) {
        String url = "https://kapi.kakao.com/v1/user/unlink";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        String body = "target_id_type=user_id&target_id=" + providerId;
        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        log.info("카카오 연동 해제 완료: {}", providerId);
    }

    private void unlinkGoogle(String accessToken) {
        if (accessToken == null) {
            log.warn("구글 access_token 없음 - 연동 해제 생략");
            return;
        }
        String url = "https://oauth2.googleapis.com/revoke?token=" + accessToken;
        restTemplate.postForObject(url, null, String.class);
        log.info("구글 연동 해제 완료");
    }

    private void unlinkNaver(String accessToken) {
        if (accessToken == null) {
            log.warn("네이버 access_token 없음 - 연동 해제 생략");
            return;
        }
        String url = "https://nid.naver.com/oauth2.0/token"
                + "?grant_type=delete"
                + "&client_id=" + naverClientId
                + "&client_secret=" + naverClientSecret
                + "&access_token=" + accessToken
                + "&service_provider=NAVER";
        restTemplate.getForObject(url, String.class);
        log.info("네이버 연동 해제 완료");
    }
}
