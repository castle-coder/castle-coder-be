package com.castle.member.controller;

import com.castle.code.error.MemberErrorCode;
import com.castle.code.success.GlobalSuccessCode;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.documentation.annotation.ApiErrorCodeExamples;
import com.castle.domain.member.entity.Member;
import com.castle.domain.member.service.MemberService;
import com.castle.member.dto.request.SignUpRequest;
import com.castle.member.dto.response.ProfileReadResponseDto;
import com.castle.response.BfResponse;
import com.castle.security.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 controller
 */
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "회원가입",
            description = "기본 회원가입."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공")
    })
    @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "EXIST_MEMBER_EMAIL")
    @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "EXIST_PHONE_NUMBER")
    @PostMapping("/sign-up")
    public ResponseEntity<BfResponse<Void>> signUp(@RequestBody SignUpRequest request) {
        // 비밀번호 암호화 및 회원가입
        memberService.signUp(request.toEntity(passwordEncoder));

        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }

    @Operation(
            summary = "회원 프로필 조회",
            description = "회원 프로필 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 프로필 조회 성공", useReturnTypeSchema = true)
    })
    @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "NOT_FOUND_MEMBER_ID")
    @GetMapping("/profiles")
    public ResponseEntity<BfResponse<ProfileReadResponseDto>> readMemberProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Member member = memberService.getMemberById(Long.parseLong(principalDetails.getUsername()));
        return ResponseEntity.ok(new BfResponse<>(ProfileReadResponseDto.from(member)));
    }

    @Operation(
            summary = "이메일 증복 확인",
            description = "회원가입 전 이미 등록되어 있는 이메일인지 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 중복 확인 성공", useReturnTypeSchema = true)
    })
    @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "EXIST_MEMBER_EMAIL")
    @GetMapping("/check-email")
    public ResponseEntity<BfResponse<Void>> checkEmailDuplicate(
            @Parameter(description = "중복확인을 받으려는 이메일 값입니다.(중복 시 error)")
            @RequestParam("email") String email) {
        memberService.isEmailDuplicate(email);
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
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
        memberService.withdraw(Long.parseLong(principalDetails.getUsername()));
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }
}
