package com.castle.core.response;

import com.castle.core.code.success.GlobalSuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import static com.castle.core.code.success.GlobalSuccessCode.SUCCESS;

/**
 * 프로젝트 전역적으로 사용되는 REST API 응답 클래스입니다.
 * @param <T>
 */
@Getter
public class BfResponse<T> {

    @Schema(description = "응답 코드", example = "200")
    private final int code;

    @Schema(description = "응답 메시지", example = "성공")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    public BfResponse(T data) {
        this.code = SUCCESS.getCode();
        this.message = SUCCESS.getMessage();
        this.data = data;
    }

    public BfResponse(GlobalSuccessCode statusCode, T data) {
        this.code = statusCode.getCode();
        this.message = statusCode.getMessage();
        this.data = data;
    }

    public BfResponse(GlobalSuccessCode statusCode) {
        this.code = statusCode.getCode();
        this.message = statusCode.getMessage();
        this.data = null;
    }
}