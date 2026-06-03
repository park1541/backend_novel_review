package com.example.backend_novel_review.config;

import com.example.backend_novel_review.auth.filter.JwtAuthenticationFilter;
import com.example.backend_novel_review.auth.handler.OAuth2FailureHandler;
import com.example.backend_novel_review.auth.handler.OAuth2SuccessHandler;
import com.example.backend_novel_review.auth.service.CustomOAuth2UserService;
import com.example.backend_novel_review.auth.service.CustomOidcUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final CustomOidcUserService oidcUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(new CorsConfig().corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/novels/**", "/api/genres").permitAll()
                .requestMatchers("/api/auth/me").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/novels/*/reviews").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reviews/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").authenticated()
                .requestMatchers("/api/users/me/**").authenticated()
                .requestMatchers("/api/auth/logout").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(e -> e.baseUri("/oauth2/authorization"))
                .redirectionEndpoint(e -> e.baseUri("/login/oauth2/code/*"))
                .userInfoEndpoint(u -> u
                    .userService(oAuth2UserService)
                    .oidcUserService(oidcUserService)
                )
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(e -> e
                .authenticationEntryPoint(
                    (req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler(
                    (req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
            );

        return http.build();
    }
}
