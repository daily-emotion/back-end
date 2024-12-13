package com.dailyemotion.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct  // 모든 의존성 주입이 완료된 후 실행됨
    public void initialize() {
        try {
            InputStream serviceAccount =
                    getClass().getResourceAsStream("/" + firebaseConfigPath);  // 루트 경로에서 시작하도록 / 추가

            if (serviceAccount == null) {
                throw new IllegalStateException("Firebase 설정 파일을 찾을 수 없습니다: " + firebaseConfigPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

        } catch (IOException e) {
            throw new RuntimeException("Firebase 초기화 중 오류 발생: ", e);
        }
    }
}