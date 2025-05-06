package com.castle.code.error;

import com.castle.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 회원 관련 에러 코드를 관리하는 enum 클래스입니다.
 */
@Getter
public enum ChatErrorCode implements BaseErrorCode {
    // 404 NOT_FOUND
    NOT_FOUND_CHAT_SESSION(404, "존재하지 않는 채팅 세션입니다.", HttpStatus.NOT_FOUND);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    ChatErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.errorCode, this.errorMessage);
    }
}