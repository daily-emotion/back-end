package com.dailyemotion.common.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TagErrorCode {
    TAG_COUNT_EXCEEDED("태그는 최대 3개까지만 선택할 수 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TAG_NAME("유효하지 않은 태그입니다.", HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus status;
}