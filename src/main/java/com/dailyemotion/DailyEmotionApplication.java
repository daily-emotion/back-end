package com.dailyemotion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class DailyEmotionApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailyEmotionApplication.class, args);
	}

}
