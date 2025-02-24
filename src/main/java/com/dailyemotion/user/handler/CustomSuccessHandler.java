package com.dailyemotion.user.handler;

import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.user.jwt.JWTUtil;
import com.dailyemotion.user.oAuth2.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 인증 성공 후의 처리를 담당하는 핸들러
 * 사용자가 소셜 로그인에 성공하면 JWT 토큰을 생성하고 프론트엔드로 리다이렉트
 */
@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;

    @Value("${jwt.accessExpiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenExpiration;

    @Value("${redirect.url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 1. SecurityContext 유지 (세션이 날아가지 않도록 설정)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. JWT 토큰 생성
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        String name = customUserDetails.getName();

        String accessToken = jwtUtil.createAccessToken(username, name, Role.USER, accessTokenExpiration);
        String refreshToken = jwtUtil.createRefreshToken(username, refreshTokenExpiration);

        // 3. 세션에 SecurityContext 저장
        HttpSession session = request.getSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // 4. 리다이렉트 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .fragment("token=" + accessToken + "&refreshToken=" + refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
