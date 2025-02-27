package com.dailyemotion.diary.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DiaryCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ObjectMapper 설정 (JSON 직렬화/역직렬화를 위한 설정)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     *   Redis에 데이터를 저장하는 메서드
     * - 데이터를 JSON 형태로 직렬화하여 저장
     * - TTL(시간 제한)을 5분(300초)으로 설정하여 일정 시간이 지나면 자동 삭제됨
     *
     * @param key    Redis에 저장할 키 (ex: "cache:getMonthlyDiaries:userId:month")
     * @param data   캐싱할 데이터 (DTO, 리스트 등)
     */
    public void setCache(String key, Object data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data); // 데이터를 JSON 문자열로 변환
            redisTemplate.opsForValue().set(key, jsonData, Duration.ofMinutes(5)); // 5분 TTL 설정 후 저장
        } catch (Exception e) {
            throw new RuntimeException("Redis 캐싱 중 오류 발생", e);
        }
    }

    /**
     *   Redis에서 데이터를 조회하는 메서드
     * - JSON 형태로 저장된 데이터를 가져와서 객체로 역직렬화함
     *
     * @param key             Redis에서 조회할 키 (ex: "cache:getMonthlyDiaries:userId:month")
     * @param typeReference   반환할 객체 타입 (예: new TypeReference<List<DiaryGetResDto>>() {})
     * @param <T>             제네릭 타입 (조회할 데이터 타입을 유연하게 설정 가능)
     * @return                캐싱된 데이터를 역직렬화하여 반환 (없으면 null)
     *
     * @note 반환타입이 TypeReference<T>인 이유는 Redis는 기본적으로 String을 사용, 객체를 다룰 때 역직렬화를 위해 사용
     * @note DTO 같은 단순객체는 Class<T>를 사용해서 처리할 수 있지만 List<DTO> 같은 객체가 연결된 객체는 TypeReference<T>를 사용해야 역직렬화가 가능
     * @note JSON -> List<DiaryGetResDto>
     *
     */
    public <T> T getCache(String key, TypeReference<T> typeReference) {
        try {
            String jsonData = (String) redisTemplate.opsForValue().get(key); // Redis에서 JSON 문자열 조회
            return jsonData != null ? objectMapper.readValue(jsonData, typeReference) : null; // JSON을 객체로 변환하여 반환
        } catch (Exception e) {
            throw new RuntimeException("Redis 캐시 조회 중 오류 발생", e);
        }
    }
}