package com.dailyemotion.common.config;

import com.dailyemotion.user.handler.CustomLogoutHandler;
import com.dailyemotion.user.handler.CustomSuccessHandler;
import com.dailyemotion.user.jwt.JWTFilter;
import com.dailyemotion.user.jwt.JWTUtil;
import com.dailyemotion.user.oAuth2.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import lombok.RequiredArgsConstructor;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomLogoutHandler customLogoutHandler;
    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS 설정 - 프론트엔드 도메인 허용
        http.cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList(
                    "https://dailyemotion.site",
                    "http://localhost:5173"
            ));
            configuration.setAllowedMethods(Collections.singletonList("*"));
            configuration.setAllowedHeaders(Collections.singletonList("*"));
            configuration.setExposedHeaders(Collections.singletonList("Authorization"));
            configuration.setMaxAge(3600L);
            return configuration;
        }));

        // 기본 보안 설정 비활성화 (JWT 사용을 위함)
        http.csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // JWT 인증 필터 추가 - OAuth2 로그인 이후 적용
        http.addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        // OAuth2 소셜 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customSuccessHandler)
        );

        // 인증 실패시 처리 - 401 Unauthorized 응답
        http.exceptionHandling(handling -> handling
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\": \"인증이 필요합니다.\"}");
                })
        );

        // URL 별 인증 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(permitAllRequestMatchers()).permitAll()  // 인증 없이 접근 가능한 경로
                .anyRequest().authenticated()  // 그 외 모든 요청은 인증 필요
        );

        // 세션 설정 - JWT 사용으로 인한 STATELESS 설정
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler((request, response, authentication) ->
                        response.setStatus(HttpServletResponse.SC_OK))
        );

        return http.build();
    }

    /**
     * 인증 없이 접근 가능한 경로 설정
     * - 회원가입/로그인 관련 (/auth/**)
     * - OAuth2 관련 (/oauth2/**)
     * - Swagger UI 관련 (API 문서)
     */
    private RequestMatcher[] permitAllRequestMatchers() {
        return new RequestMatcher[] {
                // 에러 페이지
                new AntPathRequestMatcher("/error"),

                // 인증 관련 엔드포인트
                new AntPathRequestMatcher("/auth/login", "POST"),   // 로그인
                new AntPathRequestMatcher("/auth/signup", "POST"),  // 회원가입
                new AntPathRequestMatcher("/oauth2/**"),            // 소셜 로그인

                // Swagger UI & API Docs 관련 엔드포인트
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/swagger-ui.html"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/api-docs/**"),
                new AntPathRequestMatcher("/auth/**", "POST"),

        };
    }

    /**
     * 인증이 필요한 경로 (위의 permitAll 외 모든 경로)
     * - 일기 관련 (/diary/**)
     * - 통계 관련 (/report/**)
     * - 사용자 프로필 (/user/profile)
     */

    private RequestMatcher[] authenticatedRequestMatchers() {
        return new RequestMatcher[]{
                // 사용자 관련
                antMatcher(HttpMethod.GET, "/api/users/me"),
                antMatcher(HttpMethod.PUT, "/api/users/me"),
                // 토큰 관련
                antMatcher(HttpMethod.POST, "/auth/refresh"),
                antMatcher(HttpMethod.POST, "/api/diaries/**")
                // TODO: 추가 API 엔드포인트
        };
    }
}