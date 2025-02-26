package com.dailyemotion.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host = "localhost";

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 서버와의 연결을 관리하는 Connection Factory 생성
     * - RedisStandaloneConfiguration: 단일 Redis 서버에 대한 설정
     * - LettuceConnectionFactory: 비동기 방식의 Redis 클라이언트 (Lettuce) 사용
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    /**
     * RedisTemplate 설정
     * - Redis에 데이터를 저장하고 조회하는 데 사용됨
     * - 직렬화/역직렬화 방식을 설정하여 데이터가 올바르게 저장되도록 함
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory); // Redis 연결 설정

        // JSON 직렬화를 위한 ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Java 8 날짜/시간(LocalDateTime) 지원
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 날짜를 Timestamp가 아닌 ISO 8601 문자열로 저장
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 알 수 없는 속성이 있어도 예외 발생 안 함

        // Jackson을 사용한 JSON 직렬화 설정
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // Key, HashKey는 일반 문자열로 저장 (가독성을 위해)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Value, HashValue는 JSON 직렬화 적용 (객체 저장을 위해)
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        return redisTemplate;
    }
}