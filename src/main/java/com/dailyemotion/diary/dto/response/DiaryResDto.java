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

    public static DiaryResDto from(Diary diary, List<Tag> tag) {
        return DiaryResDto.builder()
                .emotion(String.valueOf(diary.getEmotion()))
                .content(diary.getContent())
                .tag(tag.stream()
                        .map(Tag::getName) // 태그 엔티티에서 태그 이름 추출
                        .toList()) // 스트림 결과를 리스트로 반환
                .imageUrl(diary.getImageUrl())
                .build();

    }
}
