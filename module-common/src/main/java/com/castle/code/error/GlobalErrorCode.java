package com.castle.code.error;

import com.castle.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 전역적으로 공통적으로 사용되는 에러 코드를 관리하는 enum 클래스입니다.
 */
@Getter
public enum GlobalErrorCode implements BaseErrorCode {
    VALIDATION_FAILED(400, "입력 값이 유효하지 않습니다. 다시 확인해 주세요", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    GlobalErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.errorCode, this.errorMessage);
    }
}