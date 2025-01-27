package com.dailyemotion.statics.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MultiMonthSummaryRes {
    private List<MonthlyData> monthlyStats;
}
