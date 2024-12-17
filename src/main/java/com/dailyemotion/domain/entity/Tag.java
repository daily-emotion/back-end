package com.dailyemotion.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "diary_id")
    private Diary diary;

    public static Tag from(String tagName, Diary diary) {
        return Tag.builder()
                .name(tagName)
                .diary(diary)
                .build();
    }
}
