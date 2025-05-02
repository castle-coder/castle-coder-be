package com.castle.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

/**
        * JWT 로그인 실패 시 처리하는 핸들러
 * SimpleUrlAuthenticationFailureHandler를 상속받아서 구현
 */
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper; // Java 객체를 JSON으로 변환하기 위한 ObjectMapper 인스턴스

    /**
     * 인증 실패 시 호출되는 메서드
     * 인증이 실패하면 실패 이유를 ErrorResponse로 감싸서 클라이언트에 전달
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        AuthErrorCode authErrorCode = AuthErrorCode.NOT_FOUND_DB;
        // HTTP 응답 상태 코드를 400 (Bad Request)로 설정
        response.setStatus(authErrorCode.getErrorCode());
        response.setContentType("application/json"); // 응답의 Content-Type을 JSON 형식으로 설정
        response.setCharacterEncoding("utf-8");

        // 로그인 실패 시 사용할 ErrorResponse 객체 생성 (ErrorCode와 예외 메시지를 담음)
        ErrorResponse errorResponse = authErrorCode.getErrorResponse();

        // ObjectMapper를 사용하여 Response 객체를 JSON으로 직렬화하고, 클라이언트에 응답으로 보냄
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}