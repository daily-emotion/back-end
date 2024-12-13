package com.dailyemotion.firebase.controller;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/firebase/test")
public class FirebaseTestController {

    @GetMapping("/connection")
    public ResponseEntity<String> testConnection() {
        try {
            // Firebase 앱이 초기화되었는지 확인
            FirebaseApp app = FirebaseApp.getInstance();

            // Firestore 데이터베이스 참조 가져오기 (간단한 테스트용)
            Firestore db = FirestoreClient.getFirestore();

            return ResponseEntity.ok("Firebase 연결 성공! App name: " + app.getName());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Firebase 연결 실패: " + e.getMessage());
        }
    }
}