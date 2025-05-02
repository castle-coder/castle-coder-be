package com.castle.code.error;

import com.castle.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * OAuth 관련 에러 코드를 관리하는 enum 클래스입니다.
 */
@Getter
public enum AuthErrorCode implements BaseErrorCode {
    // 400 BAD_REQUEST
    NOT_FOUND_DB(400, "데이터베이스에서 사용자 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    AuthErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.errorCode, this.errorMessage);
    }
}