package com.dailyemotion.domain.entity;

import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.domain.enums.Emotion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Emotion emotion;

    private String content;

    private String imageUrl;

    private LocalDate date;

    @OneToMany(mappedBy = "diary")
    private List<Tag> tags;

    public static Diary from(DiaryReqDto diaryReqDto, User username, LocalDate date) {
        return Diary.builder()
                .user(username)
                .emotion(Emotion.valueOf(diaryReqDto.getEmotion())) // String을 Enum으로
                .content(diaryReqDto.getContent())
                .imageUrl(diaryReqDto.getImageUrl())
                .date(date) // @PathVariable값
                .build();
    }

    // 태그 추가 메서드
    public void addTags(List<String> tagNames) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        for (String tagName : tagNames) {
            Tag tag = Tag.from(tagName, this);
            this.tags.add(tag);
        }
    }

}
