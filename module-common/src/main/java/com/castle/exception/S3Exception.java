package com.castle.exception;

import com.castle.code.error.BaseErrorCode;
import lombok.Getter;

/**
 * S3 관련 예외를 처리하는 클래스입니다.
 */
@Getter
public class S3Exception extends RuntimeException {

    private final BaseErrorCode errorCode;

    public S3Exception(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}