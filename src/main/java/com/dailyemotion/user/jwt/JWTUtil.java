package com.dailyemotion.user.jwt;

import com.dailyemotion.domain.enums.Role;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JWTUtil {
    private static final String USERNAME_CLAIM = "username";
    private static final String ROLE_CLAIM = "role";
    private static final String NAME_CLAIM = "name";

    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    /**
     * JWT Utility 클래스를 초기화
     * 주어진 시크릿 키를 사용하여 JWT 서명 및 파싱에 사용할 키를 생성
     *
     * @param secret JWT 서명에 사용할 시크릿 키 값
     */
    public JWTUtil(@Value("${jwt.secret}") String secret) {
        log.info("Initializing JWTUtil with secret");
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    /**
     * 액세스 토큰을 생성
     * 사용자 식별 정보, 이름, 역할을 포함하며, 지정된 시간 후 만료
     *
     * @param username 사용자 식별자
     * @param name 사용자 이름
     * @param role 사용자 역할
     * @param expiredMs 토큰 만료 시간 (밀리초)
     * @return 생성된 JWT 액세스 토큰
     */
    public String createAccessToken(String username, String name, Role role, Long expiredMs) {
        Date now = new Date();
        return Jwts.builder()
                .claim(USERNAME_CLAIM, username)
                .claim(ROLE_CLAIM, role.name())
                .claim(NAME_CLAIM, name)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 리프레시 토큰을 생성합니다.
     * 사용자 식별자만 포함하며, 새로운 액세스 토큰 발급에 사용
     *
     * @param username 사용자 식별자
     * @param expiredMs 토큰 만료 시간 (밀리초)
     * @return 생성된 JWT 리프레시 토큰
     */
    public String createRefreshToken(String username, Long expiredMs) {
        Date now = new Date();
        return Jwts.builder()
                .claim(USERNAME_CLAIM, username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 사용자 식별자를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 식별자
     * @throws JwtException 토큰이 유효하지 않거나 파싱에 실패한 경우
     */
    public String getUsername(String token) {
        return getClaim(token, USERNAME_CLAIM);
    }

    /**
     * 토큰에서 사용자 역할을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 역할
     * @throws JwtException 토큰이 유효하지 않거나 파싱에 실패한 경우
     */
    public String getRole(String token) {
        return getClaim(token, ROLE_CLAIM);
    }

    /**
     * 토큰에서 사용자 이름을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 이름
     * @throws JwtException 토큰이 유효하지 않거나 파싱에 실패한 경우
     */
    public String getName(String token) {
        return getClaim(token, NAME_CLAIM);
    }

    /**
     * 토큰에서 지정된 클레임 값을 추출합니다.
     *
     * @param token JWT 토큰
     * @param claimName 추출할 클레임 이름
     * @return 클레임 값
     * @throws JwtException 토큰이 유효하지 않거나 파싱에 실패한 경우
     */
    private String getClaim(String token, String claimName) {
        try {
            return jwtParser.parseSignedClaims(token)
                    .getPayload()
                    .get(claimName, String.class);
        } catch (Exception e) {
            log.error("Failed to get claim: {} from token", claimName, e);
            throw new JwtException("Invalid token claim: " + claimName);
        }
    }

    /**
     * 토큰의 만료 여부를 확인합니다.
     *
     * @param token JWT 토큰
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
     */
    public boolean isExpired(String token) {
        try {
            Date expiration = jwtParser.parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Token is expired", e);
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration", e);
            return true;
        }
    }

    /**
     * 토큰의 유효성을 검증합니다.
     * 서명이 유효하고 만료되지 않았는지 확인합니다.
     *
     * @param token JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean isValidToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token validation failed: Token is expired", e);
            return false;
        } catch (JwtException e) {
            log.error("Token validation failed: Invalid token", e);
            return false;
        } catch (Exception e) {
            log.error("Token validation failed: Unexpected error", e);
            return false;
        }
    }
}