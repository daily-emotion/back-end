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
import java.util.Optional;

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

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
    private List<Tag> tags;


    //수정 메서드
    public void updateFrom(Diary updatedDiary) {
        this.emotion = updatedDiary.getEmotion();
        this.content = updatedDiary.getContent();
        this.imageUrl = updatedDiary.getImageUrl();
    }

    // ReqDto를 Diary 엔티티로 변환하는 메서드
    public static Diary fromReqDto(DiaryReqDto diaryReqDto, User user, LocalDate date) {
        return Diary.builder()
                .user(user)
                .emotion(Emotion.valueOf(diaryReqDto.getEmotion()))
                .content(diaryReqDto.getContent())
                .imageUrl(diaryReqDto.getImageUrl())
                .date(date)
                .tags(new ArrayList<>())
                .build();
    }
}
