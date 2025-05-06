package com.castle.code.error;

import com.castle.response.ErrorResponse;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역적으로 사용될 에러 코드의 공통 인터페이스입니다.
 */
public interface BaseErrorCode {
    int getErrorCode();

    String getErrorMessage();

    HttpStatus getStatus();

    ErrorResponse getErrorResponse();
}