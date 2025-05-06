package com.castle.exception;

import com.castle.code.error.BaseErrorCode;
import lombok.Getter;

/**
 * RunPodException은 런팟 관련 예외를 처리하는 클래스입니다.
 * 런팟 API 호출 시 발생할 수 있는 예외를 정의합니다.
 */
@Getter
public class RunPodException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public RunPodException(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}