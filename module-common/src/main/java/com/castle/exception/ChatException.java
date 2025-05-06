package com.castle.exception;

import com.castle.code.error.BaseErrorCode;
import lombok.Getter;

/**
 * Chat 관련 예외를 처리하는 클래스입니다.
 */
@Getter
public class ChatException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public ChatException(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}