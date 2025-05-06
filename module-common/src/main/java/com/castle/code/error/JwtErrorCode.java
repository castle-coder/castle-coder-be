package com.castle.code.error;

import com.castle.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * JWT 관련 에러 코드를 관리하는 enum 클래스입니다.
 */
@Getter
public enum JwtErrorCode implements BaseErrorCode {
    // 400 BAD_REQUEST
    INVALID_TOKEN(400, "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_FORMAT(400, "잘못된 토큰 형식입니다.", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_TOKEN(400, "지원되지 않는 토큰입니다.", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN_ARGUMENT(400, "잘못된 토큰 인수입니다.", HttpStatus.BAD_REQUEST),

    // 401 UNAUTHORIZED,
    EXPIRED_TOKEN(401, "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INSUFFICIENT_TOKEN_PERMISSIONS(401, "접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_SIGNATURE(401, "유효하지 않은 토큰 서명입니다.", HttpStatus.UNAUTHORIZED),

    // 500 INTERNAL_SERVER_ERROR
    TOKEN_PROCESSING_ERROR(500, "토큰 처리 중 서버 오류가 발생했습니다. 관리자에게 문의하세요.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    JwtErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.errorCode, this.errorMessage);
    }
}