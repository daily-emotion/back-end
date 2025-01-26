package com.dailyemotion.statics.service;


import com.dailyemotion.common.exception.StatsException;
import com.dailyemotion.diary.repository.DiaryRepository;
import com.dailyemotion.domain.entity.Diary;
import com.dailyemotion.domain.enums.Emotion;
import com.dailyemotion.statics.dto.response.EmoStatsRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dailyemotion.common.errorCode.StatsErrorCode.STATISTICS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class EmoStatsService {

    private final DiaryRepository diaryRepository;

    public EmoStatsRes getMonthlyStatistics(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Diary> diaries = diaryRepository.findByDateBetween(startDate, endDate);

        if (diaries.isEmpty()) {
            throw new StatsException(STATISTICS_NOT_FOUND);
        }

        // 감정 카운팅을 Map으로 직접 변환
        Map<Emotion, Long> emotionCounts = diaries.stream()
                .collect(Collectors.groupingBy(
                        Diary::getEmotion,
                        Collectors.counting()
                ));

        return EmoStatsRes.builder()
                .yearMonth(String.format("%d-%02d", year, month))
                .emotionCounts(emotionCounts)
                .build();
    }
}