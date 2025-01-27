package com.dailyemotion.statics.service;

import com.dailyemotion.diary.repository.DiaryRepository;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.entity.Tag;
import com.dailyemotion.statics.dto.response.TagStatsRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagStatsService {
    private final DiaryRepository diaryRepository;

    public TagStatsRes getMonthlyStatistics(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Diary> diaries = diaryRepository.findByDateBetween(startDate, endDate);


        Map<String, Long> tagCounts = diaries.stream()
                .flatMap(diary -> diary.getTags().stream())
                .collect(Collectors.groupingBy(
                        Tag::getName,
                        Collectors.counting()
                ));

        return TagStatsRes.builder()
                .yearMonth(String.format("%d-%02d", year, month))
                .tagCounts(tagCounts)
                .build();
    }
}