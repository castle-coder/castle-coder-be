package com.castle.runpod.controller;

import com.castle.code.error.MemberErrorCode;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.documentation.annotation.ApiErrorCodeExamples;
import com.castle.runpod.dto.request.CodeGenerateRequest;
import com.castle.runpod.service.RunPodStreamService;
import com.castle.security.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
@Tag(name = "AI 통신", description = "AI 통신 관련 API")
public class RunPodStreamController {

    private final RunPodStreamService runPodStreamService;

    /**
     * 프론트엔드에서 prompt 파라미터를 받아 SSE 스트림으로 응답 전송
     */
    @Operation(
            summary = "ai 모델 코드 생성 요청",
            description = "ai 모델 코드 생성 요청합니다."
    )
    @SecurityRequirements(value = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코드 생성 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "FORBIDDEN_CHAT_SESSION_ACCESS"),
    })
    @PostMapping(value = "/generate-code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateCode(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody CodeGenerateRequest request) {
        if (request.getImageUrls() != null) {
            return runPodStreamService.streamToClient(request.getPrompt(), Long.parseLong(principalDetails.getUsername()), request.getChatSessionId());
        }
        // gpt api로 이미지 변환 api 추가
        return runPodStreamService.streamToClient(request.getPrompt(), Long.parseLong(principalDetails.getUsername()), request.getChatSessionId());
    }

    /**
     * 프론트엔드에서 code prompt 파라미터를 받아 SSE 스트림으로 응답 전송
     */
    @Operation(
            summary = "ai 모델 코드 보안 리팩토링 요청",
            description = "ai 모델 코드 보안 리팩토링 요청합니다."
    )
    @SecurityRequirements(value = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코드 생성 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "FORBIDDEN_CHAT_SESSION_ACCESS"),
    })
    @CrossOrigin(origins = "http://localhost:63342")
    @PostMapping(value = "/refactor-code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter refactorCode(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody CodeGenerateRequest request) {
        return runPodStreamService.streamToClient(request.appendInstruction(), Long.parseLong(principalDetails.getUsername()), request.getChatSessionId());
    }
}
