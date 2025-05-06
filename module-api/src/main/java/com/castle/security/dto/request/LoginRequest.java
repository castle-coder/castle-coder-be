package com.castle.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * BASIC 로그인 요청에 필요한 정보를 담는 DTO 클래스입니다.
 */
@Getter
@RequiredArgsConstructor
public class LoginRequest {

    @Schema(description = "회원 이메일", example = "example@gmail.com", required = true)
    private final String email;

    @Schema(description = "비밀번호", example = "@@example1234", required = true)
    private final String password;
}