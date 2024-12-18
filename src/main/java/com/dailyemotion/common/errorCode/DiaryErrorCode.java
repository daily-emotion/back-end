package com.dailyemotion.common.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum DiaryErrorCode {

    // 에러메시지와 상태코드(HttpStatus)를 관리
    DIARY_ALREADY_EXIST ("이미 해당 날짜에 Diary가 존재합니다.", HttpStatus.CONFLICT),
    INVALID_EMOTION_VALUE("유효하지 않은 감정 값입니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;
}
