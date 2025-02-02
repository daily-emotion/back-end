package com.dailyemotion.diary.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final Storage storage;
    private static final String BUCKET_NAME = "dailyemotion_buket"; //실수로 buket..으로 지정했어요

    public String uploadImage(MultipartFile file) {
        try {
            // UUID로 고유한 파일명 생성
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 파일 정보 설정
            BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, fileName)
                    .setContentType(file.getContentType())
                    .build();

            // 파일 업로드
            storage.create(blobInfo, file.getInputStream());

            // 업로드된 파일의 공개 URL 반환
            return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, fileName);

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }
    }
}