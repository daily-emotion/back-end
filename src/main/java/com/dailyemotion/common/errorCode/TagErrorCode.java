package com.dailyemotion.common.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TagErrorCode {
    // 에러메시지와 상태코드(HttpStatus)를 관리
    TAG_NOT_FOUND("태그를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus status;
}
