package com.dailyemotion.tag.service;

import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import com.dailyemotion.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<String> createTag(Diary diary, DiaryReqDto diaryReqDto) {
        List<String> tags = diaryReqDto.getTag();
        for (String i : tags) {
            Tag tag = Tag.builder()
                    .name(i)
                    .diary(diary)
                    .build();
            tagRepository.save(tag);
        }
        return tags;
    }
}