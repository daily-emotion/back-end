package com.dailyemotion.common.exception;

import com.dailyemotion.common.errorCode.DiaryErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 발생한 예외를 캐치하고 상태코드와 메시지를 클라이언트에게 전달

    @ExceptionHandler(DiaryException.class)
    public ResponseEntity<String> handleDiaryException(DiaryException ex) {
        DiaryErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorCode.getMessage());
    }
}
