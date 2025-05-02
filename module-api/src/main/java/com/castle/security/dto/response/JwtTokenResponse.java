package com.castle.security.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JWT 액세스 토큰과 리프레시 토큰을 담는 DTO 클래스입니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class JwtTokenResponse {

    @Schema(description = "JWT 액세스 토큰", example = "eyJ...")
    private final String accessToken;

    @Schema(description = "JWT 리프레시 토큰", example = "eyJ...")
    private final String refreshToken;
}