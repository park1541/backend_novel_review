package com.example.backend_novel_review.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {

    public static void addAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(3600)
            .sameSite("None")
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .sameSite("None")
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static Optional<String> getAccessToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
            .filter(c -> "access_token".equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }
}
