package com.dailyemotion.common.exception;


import com.dailyemotion.common.errorCode.TagErrorCode;
import com.dailyemotion.common.errorCode.UserErrorCode;

public class TagException extends RuntimeException {

    private final TagErrorCode errorCode;

    public TagException(TagErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TagErrorCode getErrorCode() {
        return errorCode;
    }
}
