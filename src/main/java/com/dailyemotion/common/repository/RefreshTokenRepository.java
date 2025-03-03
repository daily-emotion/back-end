package com.dailyemotion.common.repository;

import org.springframework.data.repository.CrudRepository;
import com.dailyemotion.domain.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    // userId로 특정 사용자의 리프레시 토큰을 찾기 위한 메서드
    Optional<RefreshToken> findByUsername(String username);
}