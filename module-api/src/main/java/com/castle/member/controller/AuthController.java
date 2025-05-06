package com.castle.member.controller;

import com.castle.code.error.JwtErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.documentation.annotation.ApiErrorCodeExamples;
import com.castle.response.BfResponse;
import com.castle.security.dto.response.JwtTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/common/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증, 인가 및 토큰 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "OAuth AccessToken 발급",
            description = "OAuth 제공자 서버로부터 AccessToken을 발급받습니다. (백엔드 테스트 용도입니다.)"
    )
    @SecurityRequirements(value = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth AccessToken 발급 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(errorClass = AuthErrorCode.class, field = "OAUTH_PROVIDER_SERVER_ERROR"),
            @ApiErrorCodeExample(errorClass = AuthErrorCode.class, field = "BAD_REQUEST_OAUTH_USER_INFO")
    })
    @PostMapping("/request")
    public ResponseEntity<BfResponse<AccessTokenResponse>> getAccessToken(@RequestBody OAuthAuthorizationRequest oAuthAuthorizationRequest) {
        AccessTokenResponse accessToken = authService.getAccessToken(oAuthAuthorizationRequest);
        return ResponseEntity.ok(new BfResponse<>(accessToken));
    }

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

    @Operation(
            summary = "회원 탈퇴",
            description = "Oauth 및 기본 회원가입에 대한 회원 탈퇴 기능."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "NOT_FOUND_MEMBER_ID"),
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "CANNOT_WITHDRAW_MEMBER")
    })
    @DeleteMapping("/withdraw")
    public ResponseEntity<BfResponse<Void>> refreshTokens(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        authService.withdraw(Long.parseLong(principalDetails.getUsername()));
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }
}