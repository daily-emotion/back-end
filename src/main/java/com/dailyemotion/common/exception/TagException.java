package com.dailyemotion.common.exception;


import com.dailyemotion.common.errorCode.TagErrorCode;
import lombok.Getter;

@Getter
public class TagException extends RuntimeException {

    private final TagErrorCode errorCode;

    public TagException(TagErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
