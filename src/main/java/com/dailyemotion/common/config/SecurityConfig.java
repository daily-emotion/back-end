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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import lombok.RequiredArgsConstructor;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
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
        // CORS 설정
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    // TODO: 프론트엔드 도메인으로 변경 필요
                    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    configuration.setMaxAge(3600L);
                    configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                    return configuration;
                }));

        // 기본 설정 비활성화
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // JWT 필터 설정
        http
                .addFilterAfter(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // OAuth2 설정
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                );

        // 인증 실패 처리
        http
                .exceptionHandling(customizer ->
                        customizer.authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"인증이 필요합니다.\"}");
                        })
                );

        // URL 별 인가 설정
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permitAllRequestMatchers()).permitAll()
                        .requestMatchers(authenticatedRequestMatchers()).authenticated()
                        .anyRequest().denyAll()
                );

        // 세션 설정
        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 로그아웃 설정
        http
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                );

        return http.build();
    }

    /**
     * 인증 없이 접근 가능한 엔드포인트 설정
     */
    private RequestMatcher[] permitAllRequestMatchers() {
        return new RequestMatcher[]{
                // 인증 관련
                antMatcher(HttpMethod.POST, "/auth/login"),
                antMatcher(HttpMethod.POST, "/auth/signup"),
                antMatcher(HttpMethod.GET, "/oauth2/**"),
                // Swagger
                antMatcher(HttpMethod.GET, "/swagger-ui/**"),
                antMatcher(HttpMethod.GET, "/v3/api-docs/**"),
                // Health Check
                antMatcher(HttpMethod.GET, "/health"),
                antMatcher(HttpMethod.POST, "/auth/token"),
        };
    }

    /**
     * 인증이 필요한 엔드포인트 설정
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