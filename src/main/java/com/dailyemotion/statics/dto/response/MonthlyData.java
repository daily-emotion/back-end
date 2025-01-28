package com.dailyemotion.statics.dto.response;

import com.dailyemotion.domain.enums.Emotion;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MonthlyData {
    private String yearMonth;
    private SummaryStatsRes stats;
}