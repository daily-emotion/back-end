package com.dailyemotion.common.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StatsErrorCode {
    STATISTICS_NOT_FOUND("해당 월의 통계 데이터가 없습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus status;
}