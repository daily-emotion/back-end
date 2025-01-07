package com.dailyemotion.user.controller;

import com.dailyemotion.user.dto.response.TokenResponseDTO;
import com.dailyemotion.user.dto.response.UserInfoResponseDTO;
import com.dailyemotion.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * Access Token을 재발급하는 엔드포인트
     * OAuth2 로그인 이후 Access Token이 만료되었을 때 사용
     * 새로운 Access Token은 응답 헤더의 Authorization 필드를 통해 전달
     *
     * @param refreshToken 현재 가지고 있는 Refresh Token
     * @return 새로운 Access Token이 Authorization 헤더에 포함된 ResponseEntity
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<Void> refreshToken(@RequestHeader("RefreshToken") String refreshToken) {
        TokenResponseDTO tokenResponse = userService.refreshToken(refreshToken);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                .build();
    }

    /**
     * 현재 인증된 사용자의 프로필 정보를 조회하는 엔드포인트
     * SecurityContext에서 현재 인증된 사용자의 정보를 가져와 반환
     * 사용자의 기본 정보(name)를 포함한 DTO를 반환
     *
     * @return 사용자 프로필 정보가 담긴 UserInfoResponseDTO
     */
    @GetMapping("/profile")
    public ResponseEntity<UserInfoResponseDTO> getUserProfile() {
        UserInfoResponseDTO userInfo = userService.getUserInfo();
        return ResponseEntity.ok(userInfo);
    }
}