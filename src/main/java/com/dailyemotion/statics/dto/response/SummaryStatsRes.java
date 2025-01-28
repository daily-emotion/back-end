package com.dailyemotion.statics.dto.response;

import com.dailyemotion.domain.enums.Emotion;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class SummaryStatsRes {
    private String yearMonth;
    private List<Map.Entry<Emotion, Long>> topEmotions;  // 상위 3개 감정
    private List<Map.Entry<String, Long>> topTags;       // 상위 6개 태그
}