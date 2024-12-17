package com.dailyemotion.common.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum UserErrorCode {

    // 에러메시지와 상태코드(HttpStatus)를 관리
    USER_NOT_AUTORIZED ("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus status;
}
