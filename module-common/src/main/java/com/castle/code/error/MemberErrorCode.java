package com.castle.code.error;

import com.castle.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 회원 관련 에러 코드를 관리하는 enum 클래스입니다.
 */
@Getter
public enum MemberErrorCode implements BaseErrorCode {

    // 403 FORBIDDEN
    FORBIDDEN_DIALOGUE_ACCESS(403, "회원과 해당 대화 세션의 권한이 일치하지 않습니다.", HttpStatus.FORBIDDEN),

    // 404 NOT_FOUND
    NOT_FOUND_MEMBER_ID(404, "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),

    // 409 CONFLICT
    EXIST_MEMBER_EMAIL(409, "이미 등록되어 있는 이메일입니다.", HttpStatus.NOT_FOUND),
    EXIST_PHONE_NUMBER(409, "이미 등록되어 있는 전화번호입니다.", HttpStatus.NOT_FOUND);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    MemberErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.errorCode, this.errorMessage);
    }
}