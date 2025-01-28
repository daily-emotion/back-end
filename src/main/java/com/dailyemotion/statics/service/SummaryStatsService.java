package com.dailyemotion.statics.service;


import com.dailyemotion.domain.enums.Emotion;
import com.dailyemotion.statics.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SummaryStatsService {
    private final EmoStatsService emotionStatsService;
    private final TagStatsService tagStatsService;

    // 여러 달의 통계를 한번에 조회하는 메소드
    public MultiMonthSummaryRes getMultiMonthSummary(int year, int month) {
        List<MonthlyData> monthlyDataList = new ArrayList<>();

        // 현재 달과 이전 달의 시작일 계산
        LocalDate currentMonth = LocalDate.of(year, month, 1);
        LocalDate prevMonth = currentMonth.minusMonths(1);

        // 이전 달 데이터 생성
        MonthlyData prevMonthData = MonthlyData.builder()
                .yearMonth(String.format("%d-%02d", prevMonth.getYear(), prevMonth.getMonthValue()))
                .stats(getMonthlySummary(prevMonth.getYear(), prevMonth.getMonthValue()))
                .build();

        // 현재 달 데이터 생성
        MonthlyData currentMonthData = MonthlyData.builder()
                .yearMonth(String.format("%d-%02d", year, month))
                .stats(getMonthlySummary(year, month))
                .build();

        // 리스트에 순서대로 추가
        monthlyDataList.add(prevMonthData);
        monthlyDataList.add(currentMonthData);

        // 최종 응답 생성
        return MultiMonthSummaryRes.builder()
                .monthlyStats(monthlyDataList)
                .build();
    }

    // 한 달의 통계를 조회하는 메소드
    public SummaryStatsRes getMonthlySummary(int year, int month) {
        // 감정 통계 조회
        EmoStatsRes emotionStats = emotionStatsService.getMonthlyStatistics(year, month);
        List<Map.Entry<Emotion, Long>> topEmotions = emotionStats.getEmotionCounts().entrySet()
                .stream()
                .sorted(Map.Entry.<Emotion, Long>comparingByValue().reversed())
                .limit(3) //감정은 상위 3개
                .collect(Collectors.toList());

        // 태그 통계 조회
        TagStatsRes tagStats = tagStatsService.getMonthlyStatistics(year, month);
        List<Map.Entry<String, Long>> topTags = tagStats.getTagCounts().entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6) //태그는 상위 6개
                .collect(Collectors.toList());

        return SummaryStatsRes.builder()
                .yearMonth(String.format("%d-%02d", year, month))
                .topEmotions(topEmotions)
                .topTags(topTags)
                .build();
    }
}