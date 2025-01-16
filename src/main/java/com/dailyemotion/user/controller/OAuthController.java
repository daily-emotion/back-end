package com.dailyemotion.user.controller;

import com.dailyemotion.user.dto.request.TokenReqDto;
import com.dailyemotion.user.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final JWTUtil jwtUtil;

    @PostMapping("/token")
    public ResponseEntity<String> generateToken(@RequestBody TokenReqDto request) {
        // 요청받은 username과 role로 토큰 생성 (임시)
        String token = jwtUtil.createAccessToken(request.getUsername(), request.getName(), request.getRole(), request.getExpiredMs());
        return ResponseEntity.ok(token);
    }
}
