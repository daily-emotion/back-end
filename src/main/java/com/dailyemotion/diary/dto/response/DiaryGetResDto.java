package com.dailyemotion.diary.dto.response;

import com.dailyemotion.domain.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DiaryGetResDto implements Serializable {

    private String emotion;

    private LocalDate date;

    public static DiaryGetResDto from(Diary diary) {
        return DiaryGetResDto.builder()
                .emotion(diary.getEmotion().name()) // Enum을 String으로 변환
                .date(diary.getDate())
                .build();

    }
}
