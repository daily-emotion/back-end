package com.dailyemotion.user.handler;

import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.user.jwt.JWTUtil;
import com.dailyemotion.user.oAuth2.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
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

    // JWT 토큰 생성을 위한 유틸리티 클래스
    private final JWTUtil jwtUtil;

    // application.properties/yml에서 설정한 액세스 토큰의 만료 시간
    @Value("${jwt.accessExpiration}")
    private Long accessTokenExpiration;

    // application.properties/yml에서 설정한 리프레시 토큰의 만료 시간
    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenExpiration;

    // application.properties/yml에서 설정한 리다이렉트 URL
    @Value("${redirect.url}")
    private String redirectUrl;

    /**
     * OAuth2 인증 성공 시 호출되는 메소드
     * 이 메소드는 다음과 같은 처리를 수행:
     * 1. 인증된 사용자의 정보를 추출.
     * 2. 액세스 토큰과 리프레시 토큰을 생성.
     * 3. 생성된 토큰들을 URL 프래그먼트에 포함시켜 프론트엔드로 리다이렉트.
     * URL 프래그먼트를 사용하는 이유는 리다이렉트 과정에서 토큰이 노출되는 것을 방지하기 위함.
     * 프래그먼트(#)는 서버로 전송되지 않으며, 클라이언트 측에서만 접근 가능.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 인증 정보를 담고 있는 객체
     * @throws IOException 리다이렉트 과정에서 I/O 오류가 발생한 경우
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 인증된 사용자 정보 추출
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        String name = customUserDetails.getName();

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(username, name, Role.USER, accessTokenExpiration);
        String refreshToken = jwtUtil.createRefreshToken(username, refreshTokenExpiration);

        // 프론트엔드의 콜백 페이지로 리다이렉트, 프래그먼트에 토큰을 추가
        // 프래그먼트를 사용하여 토큰이 서버 로그나 URL 히스토리에 남지 않도록 함
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .fragment("token=" + accessToken + "&refreshToken=" + refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
