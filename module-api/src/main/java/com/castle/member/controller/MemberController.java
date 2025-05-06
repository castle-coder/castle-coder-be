package com.castle.member.controller;

import com.castle.code.success.GlobalSuccessCode;
import com.castle.domain.member.service.MemberService;
import com.castle.member.dto.request.SignUpRequest;
import com.castle.response.BfResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/common/members")
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
    @PostMapping("/sign-up")
    public ResponseEntity<BfResponse<Void>> signUp(@RequestBody SignUpRequest request) {
        // 비밀번호 암호화 및 회원가입
        memberService.signUp(request.toEntity(passwordEncoder));

        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }
}
