package com.dailyemotion.user.controller;

import com.dailyemotion.user.dto.response.TokenResponseDTO;
import com.dailyemotion.user.dto.response.UserInfoResponseDTO;
import com.dailyemotion.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.dailyemotion.common.utill.SecurityUtilsName.getCustomOAuth2Name;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "1. User Controller", description = "User API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Access Token 재발급",
            description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<Void> refreshToken(
            @Parameter(description = "Refresh Token", required = true)
            @RequestHeader("RefreshToken") String refreshToken) {
        TokenResponseDTO tokenResponse = userService.refreshToken(refreshToken);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                .build();
    }

    @Operation(summary = "사용자 프로필 조회",
            description = "현재 인증된 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })

    @GetMapping("/profile")
    public ResponseEntity<UserInfoResponseDTO> getUserProfile() {
        String name = getCustomOAuth2Name(); // 현재 사용자 이름 가져오기
        UserInfoResponseDTO userInfo = userService.getUserInfo(name);
        return ResponseEntity.ok(userInfo);
    }
}