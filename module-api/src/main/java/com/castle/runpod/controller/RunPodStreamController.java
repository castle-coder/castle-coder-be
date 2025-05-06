package com.castle.runpod.controller;

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
@RequestMapping("/runpod")
@RequiredArgsConstructor
@Tag(name = "AI 통신", description = "AI 통신 관련 API")
public class RunPodStreamController {

    private final RunPodStreamService runPodStreamService;

    /**
     * 프론트엔드에서 prompt 파라미터를 받아 SSE 스트림으로 응답 전송
     */
    @Operation(
            summary = "ai 모델 응답 요청",
            description = "헬스 체킹을 진행합니다."
    )
    @SecurityRequirements(value = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "헬스 체크 성공", useReturnTypeSchema = true),
    })
    @CrossOrigin(origins = "http://localhost:63342")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter codeGenerate(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody CodeGenerateRequest request) {
        return runPodStreamService.streamToClient(request.getPrompt(), Long.parseLong(principalDetails.getUsername()));
    }
}
