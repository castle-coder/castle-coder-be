package com.castle.exception;

import com.castle.code.error.BaseErrorCode;
import lombok.Getter;

/**
 * JWT 관련 예외를 처리하는 클래스입니다.
 */
@Getter
public class JwtException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public JwtException(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}