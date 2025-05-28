package com.castle.code;

import com.castle.code.error.BaseErrorCode;
import com.castle.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 상품 에러 코드를 관리하는 enum 클래스입니다.
 */
@Getter
public enum RedisErrorCode implements BaseErrorCode {

    FAILED_TO_ADD_DATA(500, "데이터를 추가하는데 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_TO_ADD_EMAIL_DATA(500, "이메일 검증용 인증코드 생성에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_TO_READ_DATA(500, "데이터를 읽어오는데 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);


    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    RedisErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.errorCode, this.errorMessage);
    }
}
