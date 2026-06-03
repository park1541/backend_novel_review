package com.example.backend_novel_review.auth.service;

import com.example.backend_novel_review.auth.dto.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpirationMs;

    public JwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-expiration-ms}") long accessExpirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
    }

    public String generateAccessToken(UserPrincipal principal) {
        return Jwts.builder()
            .subject(String.valueOf(principal.getId()))
            .claim("email", principal.getEmail())
            .claim("nickname", principal.getNickname())
            .claim("role", principal.getRole())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
            .signWith(secretKey)
            .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
