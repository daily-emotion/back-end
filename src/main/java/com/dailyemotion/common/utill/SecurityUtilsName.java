package com.dailyemotion.common.utill;

import com.dailyemotion.common.exception.UserException;
import com.dailyemotion.user.oAuth2.CustomOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static com.dailyemotion.common.errorCode.UserErrorCode.USER_NOT_AUTHORIZED;

@Slf4j
public class SecurityUtilsName {

    // OAuth2 인증된 사용자의 username을 가져오는 메소드
    public static String getCustomOAuth2Name() {
        {
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
}