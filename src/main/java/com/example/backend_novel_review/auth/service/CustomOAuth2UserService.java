package com.example.backend_novel_review.auth.service;

import com.example.backend_novel_review.auth.dto.UserPrincipal;
import com.example.backend_novel_review.user.dto.User;
import com.example.backend_novel_review.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;

    // 네이버, 카카오 (일반 OAuth2)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());
        User user = saveOrUpdate(attributes, accessToken);

        return UserPrincipal.of(user, oAuth2User.getAttributes());
    }

    public User saveOrUpdate(OAuthAttributes attributes, String accessToken) {
        return userMapper.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
            .map(existing -> {
                User updated = existing.update(attributes.getNickname(), attributes.getProfileImageUrl(), accessToken);
                userMapper.update(updated);
                return updated;
            })
            .orElseGet(() -> {
                User newUser = attributes.toEntity(accessToken);
                userMapper.save(newUser);
                return userMapper.findByProviderAndProviderId(
                    attributes.getProvider(), attributes.getProviderId()).orElseThrow();
            });
    }
}
