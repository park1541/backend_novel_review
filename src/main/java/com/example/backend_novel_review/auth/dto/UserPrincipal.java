package com.example.backend_novel_review.auth.dto;

import com.example.backend_novel_review.user.dto.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class UserPrincipal implements OidcUser, OAuth2User {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String role;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    private UserPrincipal(Long id, String email, String nickname, String role,
                          Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.attributes = attributes;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    public static UserPrincipal of(User user, Map<String, Object> attributes) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getNickname(),
            user.getRole(), attributes, null, null);
    }

    public static UserPrincipal ofOidc(User user, Map<String, Object> attributes,
                                       OidcIdToken idToken, OidcUserInfo userInfo) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getNickname(),
            user.getRole(), attributes, idToken, userInfo);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }
}
