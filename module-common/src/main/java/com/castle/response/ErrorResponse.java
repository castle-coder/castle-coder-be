package com.castle.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.castle.code.error.GlobalErrorCode.VALIDATION_FAILED;

/**
 * 프로젝트 전역적으로 사용되는 REST API 에러 응답 클래스입니다.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "400")
    private final int errorCode;

    @Schema(description = "에러 메시지", example = "실패")
    private final String errorMessage;

    private List<CustomFieldError> customFieldErrors;

    private List<CustomFieldError> constraintMessages;

    public ErrorResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static ResponseEntity<ErrorResponse> toResponseEntityWithErrors(Errors errors) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(VALIDATION_FAILED.getErrorCode())
                .errorMessage(VALIDATION_FAILED.getErrorMessage())
                .build();

        errorResponse.setCustomFieldErrors(errors.getFieldErrors());

        return ResponseEntity
                .status(VALIDATION_FAILED.getErrorCode())
                .body(errorResponse);
    }

    public static ResponseEntity<ErrorResponse> toResponseEntityWithConstraints(Set<ConstraintViolation<?>> violations) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(VALIDATION_FAILED.getErrorCode())
                .errorMessage(VALIDATION_FAILED.getErrorMessage())
                .build();

        errorResponse.setConstraintMessages(violations);

        return ResponseEntity
                .status(VALIDATION_FAILED.getErrorCode())
                .body(errorResponse);
    }


    private void setCustomFieldErrors(List<FieldError> fieldErrors) {

        customFieldErrors = new ArrayList<>();

        fieldErrors.forEach(error -> {
            customFieldErrors.add(new CustomFieldError(
                    error.getField(),
                    error.getRejectedValue(),
                    error.getDefaultMessage()
            ));
        });
    }

    private void setConstraintMessages(Set<ConstraintViolation<?>> violations) {

        customFieldErrors = new ArrayList<>();

        violations.forEach(error -> {
            customFieldErrors.add(new CustomFieldError(
                    error.getPropertyPath().toString(),
                    error.getInvalidValue(),
                    error.getMessage()
            ));
        });
    }

    @Getter
    @Builder
    public static class CustomFieldError {

        private String field;
        private Object value;
        private String reason;

        public CustomFieldError(String field, Object value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

    }
}
