package com.example.backend_novel_review.auth.service;

import com.example.backend_novel_review.auth.dto.UserPrincipal;
import com.example.backend_novel_review.user.domain.User;
import com.example.backend_novel_review.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // 네이버, 카카오 (일반 OAuth2)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());
        User user = saveOrUpdate(attributes);

        return UserPrincipal.of(user, oAuth2User.getAttributes());
    }

    public User saveOrUpdate(OAuthAttributes attributes) {
        return userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
            .map(existing -> {
                User updated = existing.update(attributes.getNickname(), attributes.getProfileImageUrl());
                userRepository.update(updated);
                return updated;
            })
            .orElseGet(() -> {
                User newUser = attributes.toEntity();
                userRepository.save(newUser);
                return userRepository.findByProviderAndProviderId(
                    attributes.getProvider(), attributes.getProviderId()).orElseThrow();
            });
    }
}
