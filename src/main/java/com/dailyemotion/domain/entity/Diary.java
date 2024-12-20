package com.dailyemotion.domain.entity;

import com.dailyemotion.domain.enums.Emotion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    private String tag;

    private LocalDate date;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
    private List<Tag> tags;
}
