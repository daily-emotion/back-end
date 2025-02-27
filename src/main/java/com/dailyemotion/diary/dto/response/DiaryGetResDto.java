package com.dailyemotion.diary.dto.response;

import com.dailyemotion.domain.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DiaryGetResDto {

    private String emotion;

    private LocalDate date;

    public static DiaryGetResDto from(Diary diary) {
        return DiaryGetResDto.builder()
                .emotion(diary.getEmotion().name()) // Enum을 String으로 변환
                .date(diary.getDate())
                .build();

    }
}
