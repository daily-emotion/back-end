package com.dailyemotion.statics.service;


import com.dailyemotion.domain.enums.Emotion;
import com.dailyemotion.statics.dto.response.EmoStatsRes;
import com.dailyemotion.statics.dto.response.SummaryStatsRes;
import com.dailyemotion.statics.dto.response.TagStatsRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryStatsService {

    private final EmoStatsService emotionStatsService;
    private final TagStatsService tagStatsService;

    public SummaryStatsRes getMonthlySummary(int year, int month) {
        // 감정 통계 (상위 3개)
        EmoStatsRes emotionStats = emotionStatsService.getMonthlyStatistics(year, month);
        List<Map.Entry<Emotion, Long>> topEmotions = emotionStats.getEmotionCounts().entrySet()
                .stream()
                .sorted(Map.Entry.<Emotion, Long>comparingByValue().reversed()) // 값이 큰 순서대로 정렬
                .limit(3) // 상위 3개만 선택
                .collect(Collectors.toList());

        // 태그 통계 (상위 6개)
        TagStatsRes tagStats = tagStatsService.getMonthlyStatistics(year, month);
        List<Map.Entry<String, Long>> topTags = tagStats.getTagCounts().entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // 값이 큰 순서대로 정렬
                .limit(6) // 상위 6개만 선택
                .collect(Collectors.toList());

        // SummaryStatsRes도 수정이 필요합니다
        return SummaryStatsRes.builder()
                .yearMonth(String.format("%d-%02d", year, month))
                .topEmotions(topEmotions)    // 이름을 변경하고 List 타입으로 변경
                .topTags(topTags)            // 이름을 변경하고 List 타입으로 변경
                .build();
    }
}

// SummaryStatsRes도 새로운 구조에 맞게 수정해야 합니다
