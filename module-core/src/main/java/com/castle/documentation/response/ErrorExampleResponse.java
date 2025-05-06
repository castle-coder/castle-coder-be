package com.castle.documentation.response;

import com.castle.code.error.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "from")
public class ErrorExampleResponse {
    private int errorCode;
    private String errorMessage;

    public static ErrorExampleResponse from(BaseErrorCode errorCode) {
        return new ErrorExampleResponse(
                errorCode.getErrorCode(),
                errorCode.getErrorMessage()
        );
    }
}