package com.dailyemotion.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Spring의 캐시 기능을 활성화하는 Config
    // @Cacheable, @CacheEvict 등의 어노테이션을 사용할 수 있게 해준다.
}
