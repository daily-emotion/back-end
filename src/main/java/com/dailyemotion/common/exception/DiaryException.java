package com.dailyemotion.common.exception;

import com.dailyemotion.common.errorCode.DiaryErrorCode;

public class DiaryException extends RuntimeException {
    // 에러 코드를 기반으로 예외를 터트림

    private final DiaryErrorCode errorCode;

    public DiaryException(DiaryErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DiaryErrorCode getErrorCode() {
        return errorCode;
    }
}
