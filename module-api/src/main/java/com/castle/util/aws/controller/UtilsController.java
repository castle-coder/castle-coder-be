package com.castle.util.aws.controller;

import com.castle.code.success.GlobalSuccessCode;
import com.castle.response.BfResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/utils")
@RequiredArgsConstructor
@Tag(name = "유틸리티", description = "유틸리티 관련 API")
public class UtilsController {

    @Operation(
            summary = "헬스 체크",
            description = "헬스 체킹을 진행합니다."
    )
    @SecurityRequirements(value = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "헬스 체크 성공", useReturnTypeSchema = true),
    })
    @GetMapping("/health")
    public ResponseEntity<BfResponse<GlobalSuccessCode>> checkHealth() {
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }
}
