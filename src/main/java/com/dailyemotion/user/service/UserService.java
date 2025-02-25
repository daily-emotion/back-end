package com.dailyemotion.user.service;

import com.dailyemotion.common.errorCode.UserErrorCode;
import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.domain.entity.User;
import com.dailyemotion.domain.enums.Role;
import com.dailyemotion.user.repository.UserRepository;
import com.dailyemotion.user.dto.response.TokenResponseDTO;
import com.dailyemotion.user.dto.response.UserInfoResponseDTO;
import com.dailyemotion.user.jwt.JWTUtil;
import com.dailyemotion.user.oAuth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.dailyemotion.common.errorCode.UserErrorCode.TOKEN_IS_NOT_VALID;
import static com.dailyemotion.common.errorCode.UserErrorCode.USER_NOT_AUTHORIZED;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.accessExpiration}") // 30분
    private Long accessTokenExpiration;

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 토큰을 재발급 하는 메소드
     */
    public TokenResponseDTO refreshToken(String refreshToken) {
        // 리프레시 토큰 검증
        if (!jwtUtil.isValidToken(refreshToken)) {
            throw new UserException(TOKEN_IS_NOT_VALID);
        }

        // 블랙리스트에 해당 refreshToken이 있다면 토큰 발급 X
        String blackListKey = "blacklist:refreshToken:" + refreshToken;
        Boolean isBlacklisted = redisTemplate.hasKey(blackListKey);
        if (Boolean.TRUE.equals(isBlacklisted)) {
            throw new UserException(TOKEN_IS_NOT_VALID);
        }

        // 리프레시 토큰에서 사용자 정보 추출
        String username = jwtUtil.getUsername(refreshToken);

        // DB에서 사용자 정보 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 새로운 엑세스 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(
                username,
                user.getName(),
                Role.USER,
                accessTokenExpiration
        );
        return new TokenResponseDTO(newAccessToken);
    }

    public UserInfoResponseDTO getUserInfo() {
        String name = getCustomOAuth2User();
        UserInfoResponseDTO responseDTO = UserInfoResponseDTO.builder()
                .name(name)
                .build();
        return responseDTO;
    }

    private static String getCustomOAuth2User() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.debug("인증 정보가 없습니다");
            throw new UserException(USER_NOT_AUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        // 안전한 타입 체크 후 처리
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            log.debug("CustomOAuth2User로부터 사용자 정보를 추출합니다: {}",
                    customOAuth2User.getName());
            return customOAuth2User.getName();
        }

        // JWT 토큰에서 추출한 사용자 정보 처리
        if (principal instanceof Map) {
            log.debug("JWT 토큰으로부터 사용자 정보를 추출합니다");
            @SuppressWarnings("unchecked")
            Map<String, Object> principalMap = (Map<String, Object>) principal;
            return (String) principalMap.get("username");
        }

        log.debug("지원하지 않는 Principal 타입입니다: {}",
                principal != null ? principal.getClass().getName() : "null");
        throw new UserException(USER_NOT_AUTHORIZED);
    }
}
