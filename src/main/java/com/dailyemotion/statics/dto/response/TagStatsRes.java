package com.dailyemotion.statics.dto.response;

import com.dailyemotion.domain.entity.Tag;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class TagStatsRes {
    private String yearMonth;
    private Map<String,Long> tagCounts;
}