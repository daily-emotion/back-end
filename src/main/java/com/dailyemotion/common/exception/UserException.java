package com.dailyemotion.common.exception;

import com.dailyemotion.common.errorCode.UserErrorCode;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {

    private final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
