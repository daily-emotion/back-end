package com.dailyemotion.common.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum UserErrorCode {

    // 에러 메시지와 상태 코드를 관리
    USER_NOT_AUTHORIZED("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_MATCHED("사용자가 다이어리의 작성자가 아닙니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TOKEN_IS_NOT_VALID("유효하지 않은 토큰 값입니다", HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus status;
}
