package com.castle.code.error;

import com.castle.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * RunPodErrorCode는 런팟 관련 에러 코드를 관리하는 enum 클래스입니다.
 * 런팟 API 호출 시 발생할 수 있는 예외를 정의합니다.
 */
@Getter
public enum RunPodErrorCode implements BaseErrorCode {
    RUNPOD_API_TIMEOUT(8001, "RunPod API 호출이 시간 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT),
    RUNPOD_API_UNAUTHORIZED(8002, "RunPod API 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    RUNPOD_API_NOT_FOUND(8003, "RunPod 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    RUNPOD_API_INTERNAL_ERROR(8004, "RunPod 서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    RUNPOD_CONNECTION_FAILED(8005, "RunPod 서버에 연결할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    RUNPOD_INVALID_RESPONSE(8006, "RunPod API에서 유효하지 않은 응답을 반환했습니다.", HttpStatus.BAD_GATEWAY);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    RunPodErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.errorCode, this.errorMessage);
    }
}