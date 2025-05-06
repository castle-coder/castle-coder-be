package com.castle.member.controller;

import com.castle.code.error.JwtErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.response.ErrorResponse;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.documentation.annotation.ApiErrorCodeExamples;
import com.castle.response.BfResponse;
import com.castle.security.dto.request.LoginRequest;
import com.castle.security.dto.request.RefreshTokenRequest;
import com.castle.security.dto.response.JwtTokenResponse;
import com.castle.security.service.jwt.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증, 인가 및 토큰 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "로그인",
            description = "아이디 비밀번호를 검증하여 로그인하고 JWT 토큰을 발급합니다."
    )
    @SecurityRequirements(value = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT 토큰 발급 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "JWT 토큰 발급 시 잘못된 요청으로 인해 반환되는 에러 메시지입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/login")
    public ResponseEntity<BfResponse<JwtTokenResponse>> login(@RequestBody LoginRequest loginRequest) {
        return null; // 스웨거용(로그인은 필터에서 처리)
    }

    @Operation(
            summary = "토큰 갱신",
            description = "기존 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT 토큰 갱신 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(errorClass = JwtErrorCode.class, field = "INVALID_TOKEN"),
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "NOT_FOUND_MEMBER_ID")
    })
    @PostMapping("/refresh")
    public ResponseEntity<BfResponse<JwtTokenResponse>> refreshTokens(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        JwtTokenResponse jwtTokenDto = authService.refreshTokens(refreshTokenRequest);
        return ResponseEntity.ok(new BfResponse<>(jwtTokenDto));
    }
}