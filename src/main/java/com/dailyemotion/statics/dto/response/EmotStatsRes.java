package com.dailyemotion.statics.dto.response;


import com.dailyemotion.domain.enums.Emotion;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class EmotStatsRes {
    private String yearMonth;
    private Map<Emotion, Long> emotionCounts;
}
