package com.castle.security.filter;

import com.castle.security.service.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

import static org.springframework.util.StringUtils.hasText;

/**
 * 클라이언트 요청에서 JWT 토큰을 추출하고 검증하여 인증된 사용자의 접근을 허용하는 필터 클래스입니다.
 */
@RequiredArgsConstructor
public class AuthenticatedJwtFilter extends OncePerRequestFilter {

    private static final String AUTHENTICATION_HEADER = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (hasText(token)) {
                Authentication authentication = jwtService.createAuthentication(token);

                // 1. 토큰 유효성 검증
                jwtService.validateToken(token);

                // 2. 권한 비교
                jwtService.compareAuthorities(authentication);

                // SecurityContext 에 인증 객체 설정
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);

                SecurityContextHolder.setContext(context);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // TODO: Prod 에서는 삭제
            e.printStackTrace();

            // 예외 처리
            BaseErrorCode errorCode = determineErrorCode(e);

            response.setStatus(errorCode.getStatus().value()); // 상태 코드 설정
            response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 응답의 Content-Type 설정

            // JSON 직렬화
            String responseMessage = objectMapper.writeValueAsString(errorCode.getErrorResponse());
            response.getWriter().write(responseMessage);
        }
    }

    /**
     * 예외에 따른 오류 코드 설정
     */
    private BaseErrorCode determineErrorCode(Exception e) {
        if (e instanceof MalformedJwtException) {
            // 잘못된 토큰 형식
            return JwtErrorCode.INVALID_TOKEN_FORMAT;
        } else if (e instanceof ExpiredJwtException) {
            // 토큰 만료
            return JwtErrorCode.EXPIRED_TOKEN;
        } else if (e instanceof UnsupportedJwtException) {
            // 지원하지 않는 토큰
            return JwtErrorCode.UNSUPPORTED_TOKEN;
        } else if (e instanceof SignatureException) {
            // 서명이 유효하지 않은 토큰
            return JwtErrorCode.INVALID_TOKEN_SIGNATURE;
        } else if (e instanceof IllegalArgumentException) {
            // 예상치 못한 인수가 전달됨
            return JwtErrorCode.INVALID_TOKEN_ARGUMENT;
        } else if (e instanceof AuthenticationCredentialsNotFoundException) {
            // 유효하지 않은 토큰
            return JwtErrorCode.INVALID_TOKEN;
        } else if (e instanceof AccessDeniedException) {
            // 접근 권한이 없음
            return JwtErrorCode.INSUFFICIENT_TOKEN_PERMISSIONS;
        } else {
            // 그 외의 에러
            return JwtErrorCode.TOKEN_PROCESSING_ERROR;
        }
    }

    /**
     * JWT 토큰 추출
     */
    public String extractToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHENTICATION_HEADER);

        if (hasText(token) && token.startsWith(AUTHENTICATION_SCHEME)) {
            return token.substring(AUTHENTICATION_SCHEME.length());
        }

        throw new AuthenticationCredentialsNotFoundException("토큰이 존재하지 않습니다.");
    }
}