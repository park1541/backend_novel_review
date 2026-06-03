package com.example.backend_novel_review.auth.handler;

import com.example.backend_novel_review.auth.dto.UserPrincipal;
import com.example.backend_novel_review.auth.service.JwtService;
import com.example.backend_novel_review.auth.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(principal);

        CookieUtil.addAccessTokenCookie(response, accessToken);
        response.sendRedirect(frontendUrl + "/");
    }
}
