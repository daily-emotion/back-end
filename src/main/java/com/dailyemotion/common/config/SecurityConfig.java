package com.dailyemotion.common.config;

import com.dailyemotion.user.handler.CustomLogoutHandler;
import com.dailyemotion.user.handler.CustomSuccessHandler;
import com.dailyemotion.user.jwt.JWTFilter;
import com.dailyemotion.user.jwt.JWTUtil;
import com.dailyemotion.user.oAuth2.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import lombok.RequiredArgsConstructor;

import org.springframework.security.web.util.matcher.RequestMatcher;




@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 필요한 서비스와 유틸리티 주입
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomLogoutHandler customLogoutHandler;
    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 기본 보안 설정 비활성화
        configureBasicSecurity(http);

        // JWT 필터 설정
        configureJwtFilter(http);

        // OAuth2 설정
        configureOAuth2(http);

        // 예외 처리 설정
        configureExceptionHandling(http);

        // 인증/인가 설정
        configureAuthorization(http);

        // 세션 설정
        configureSession(http);

        // 로그아웃 설정
        configureLogout(http);

        return http.build();
    }

    // 기본 보안 설정을 비활성화하는 메서드
    private void configureBasicSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());
    }

    // JWT 필터 설정을 추가하는 메서드
    private void configureJwtFilter(HttpSecurity http) {
        http.addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);
    }

    // OAuth2 로그인 설정을 구성하는 메서드
    private void configureOAuth2(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customSuccessHandler)
        );
    }

    // 인증/인가 예외 처리를 설정하는 메서드
    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(handling -> handling
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\": \"인증이 필요합니다.\", \"path\": \""
                            + request.getRequestURI() + "\"}");
                })
        );
    }

    // URL별 인증/인가 규칙을 설정하는 메서드
    private void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(permitAllRequestMatchers()).permitAll()
                .requestMatchers(authenticatedRequestMatchers()).authenticated()
                .anyRequest().denyAll()
        );
    }

    // 인증 없이 접근 가능한 URL 패턴을 정의하는 메서드
    private RequestMatcher[] permitAllRequestMatchers() {
        return new RequestMatcher[] {
                // Swagger UI 관련 경로
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/swagger-ui.html"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/api-docs/**"),
                new AntPathRequestMatcher("/auth/**", "POST"),

                // OAuth 관련 경로
                new AntPathRequestMatcher("/oauth/callback/**"),
                new AntPathRequestMatcher("/login/oauth2/code/**"),
                new AntPathRequestMatcher("/error"),
                new AntPathRequestMatcher("/oauth2/authorization/**"),

                // 에러 페이지
                new AntPathRequestMatcher("/error")
        };
    }

    // 인증이 필요한 API 엔드포인트들을 정의하는 메서드
    private RequestMatcher[] authenticatedRequestMatchers() {
        return new RequestMatcher[] {
                // 일기 관련 API
                new AntPathRequestMatcher("/diaries/images"),
                new AntPathRequestMatcher("/diaries/monthly/{month}"),
                new AntPathRequestMatcher("/diaries/{date}", "GET"),
                new AntPathRequestMatcher("/diaries/{date}", "POST"),
                new AntPathRequestMatcher("/diaries/{date}", "PUT"),
                new AntPathRequestMatcher("/diaries/{date}", "DELETE"),

                // 사용자 관련 API
                new AntPathRequestMatcher("/user/profile"),
                new AntPathRequestMatcher("/token/refresh"),
                new AntPathRequestMatcher("/token"),

                // 리포트(통계) 관련 API
                new AntPathRequestMatcher("/reports/emotions/{year}/{month}"),
                new AntPathRequestMatcher("/reports/tags/{year}/{month}"),
                new AntPathRequestMatcher("/reports/summary/{year}/{month}"),


                // 태그 관련 API
                new AntPathRequestMatcher("/tags")
        };
    }

    // 세션 관리 설정을 구성하는 메서드
    private void configureSession(HttpSecurity http) throws Exception {
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
    }

    // 로그아웃 설정을 구성하는 메서드
    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutUrl("/api/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler((request, response, authentication) ->
                        response.setStatus(HttpServletResponse.SC_OK))
        );
    }
}