package com.castle.exception;

import com.castle.code.error.BaseErrorCode;
import com.castle.response.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 핸들러 클래스입니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 관련 예외 Handler
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity handleValidationException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException 발생");
        return ErrorResponse.toResponseEntityWithErrors(ex.getBindingResult());
    }

    /**
     * ConstraintViolationException 관련 예외 Handler
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("ConstraintViolationException 발생");
        return ErrorResponse.toResponseEntityWithConstraints(ex.getConstraintViolations());
    }

    /**
     * Jwt 관련 예외 Handler
     */
    @ExceptionHandler(JwtException.class)
    protected ResponseEntity<ErrorResponse> handleMemberException(JwtException ex) {
        log.error("JwtException 발생");
        log.error("errCode = " + ex.getErrorCode() + " errorMessage = " + ex.getMessage());
        BaseErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus()).body(errorCode.getErrorResponse());
    }

    /**
     * Member 관련 예외 Handler
     */
    @ExceptionHandler(MemberException.class)
    protected ResponseEntity<ErrorResponse> handleMemberException(MemberException ex) {
        log.error("MemberException 발생");
        log.error("errCode = " + ex.getErrorCode() + " errorMessage = " + ex.getMessage());
        BaseErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus()).body(errorCode.getErrorResponse());
    }

}