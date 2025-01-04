package com.dailyemotion.user.jwt;

import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.user.dto.response.UserResDto;
import com.dailyemotion.user.oAuth2.CustomOAuth2User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 토큰 기반의 인증을 처리하는 Filter
 * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 인증 정보를 설정
 */
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    /**
     * JWT 검증이 필요 없는 공개 경로 목록
     * 소셜 로그인 및 인증 관련 엔드포인트가 포함
     */
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/login",
            "/oauth2/authorization",
            "/login/oauth2/code"
    );

    /**
     * HTTP 요청에 대한 JWT 필터링을 수행
     * 토큰 검증 및 인증 정보 설정을 처리
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 처리 중 오류 발생 시
     * @throws IOException 입출력 처리 중 오류 발생 시
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (isPublicPath(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = extractToken(request);
            if (token == null || !isValidToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            Authentication auth = createAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handleException(response, e);
        }
    }

    /**
     * 요청 URI가 공개 경로에 해당하는지 확인
     *
     * @param request HTTP 요청
     * @return 공개 경로이면 true, 아니면 false
     */
    private boolean isPublicPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return EXCLUDED_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출
     * Authorization 헤더에서 Bearer 토큰을 파싱
     *
     * @param request HTTP 요청
     * @return 추출된 JWT 토큰, 토큰이 없거나 형식이 잘못된 경우 null
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7); //(prefix)를 제거하고 실제 토큰 값만 반환)
    }

    /**
     * JWT 토큰의 유효성을 검증
     * 토큰이 만료되지 않았고 올바른 형식인지 확인
     *
     * @param token JWT 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    private boolean isValidToken(String token) {
        return !jwtUtil.isExpired(token) && jwtUtil.isValidToken(token);
    }

    /**
     * JWT 토큰에서 추출한 정보로 인증 객체를 생성
     * 토큰에서 사용자 정보를 추출하여 CustomOAuth2User 기반의 인증을 생성
     *
     * @param token JWT 토큰
     * @return 생성된 Authentication 객체
     * @throws IllegalStateException 지원하지 않는 역할인 경우
     */
    private Authentication createAuthentication(String token) {
        String username = jwtUtil.getUsername(token);
        String roleName = jwtUtil.getRole(token);
        String name = jwtUtil.getName(token);
        Role role = Role.valueOf(roleName);

        if (role != Role.USER) {
            throw new IllegalStateException("Unsupported role: " + role);
        }

        UserResDto userResDto = UserResDto.builder()
                .username(username)
                .name(name)
                .role(role)
                .build();

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userResDto);

        return new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                customOAuth2User.getAuthorities()
        );
    }

    /**
     * 필터 처리 중 발생한 예외를 처리
     * 클라이언트에게 적절한 에러 응답을 전송
     *
     * @param response HTTP 응답
     * @param e 발생한 예외
     * @throws IOException 응답 작성 중 오류 발생 시
     */
    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> error = new HashMap<>();
        error.put("error", "Authentication failed");
        error.put("message", e.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}