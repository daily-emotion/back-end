package com.dailyemotion.tag.service;

import com.dailyemotion.diary.dto.request.DiaryReqDto;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import com.dailyemotion.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<String> createTag(Diary diary, DiaryReqDto diaryReqDto) {
        List<Tag> tags = diaryReqDto.getTag().stream()
                .map(tagName -> Tag.builder()
                        .name(tagName)
                        .diary(diary)
                        .build())
                .collect(Collectors.toList());

        tagRepository.saveAll(tags);

        return diaryReqDto.getTag();
    }
}