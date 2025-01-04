package com.dailyemotion.user.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 클라이언트로부터 리프레시 토큰을 받아와서 처리
        String refreshToken = request.getHeader("RefreshToken");

        if (refreshToken != null) {
            // Redis에 리프레시 토큰을 0초 유효기간으로 설정하여 즉시 만료
            redisTemplate.opsForValue().set(refreshToken, "loggedOut", 1, TimeUnit.SECONDS);
            // 0은 RuntimeException 발생으로 1로 변경
        }
    }
}
