package com.dailyemotion.common.exception;

import com.dailyemotion.common.errorCode.DiaryErrorCode;
import com.dailyemotion.common.errorCode.UserErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.View;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final View error;

    public GlobalExceptionHandler(View error) {
        this.error = error;
    }
    // 발생한 예외를 캐치하고 상태코드와 메시지를 클라이언트에게 전달

    @ExceptionHandler(DiaryException.class)
    public ResponseEntity<String> handleDiaryException(DiaryException ex) {
        DiaryErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorCode.getMessage());
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<String> handleUserException(UserException ex) {
        UserErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorCode.getMessage());
    }
}
