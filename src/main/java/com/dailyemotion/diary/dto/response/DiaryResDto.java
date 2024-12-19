package com.dailyemotion.diary.dto.response;

import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResDto {

    private String emotion;

    private String content;

    private List<String> tag;

    private String imageUrl;

    public static DiaryResDto from(Diary diary, List<String> tags) {
        return DiaryResDto.builder()
                .emotion(diary.getEmotion().name())
                .content(diary.getContent())
                .tag(tags)
                .imageUrl(diary.getImageUrl())
                .build();

    }
}
