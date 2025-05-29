package com.castle.runpod.controller;

import com.castle.code.error.MemberErrorCode;
import com.castle.code.success.GlobalSuccessCode;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.documentation.annotation.ApiErrorCodeExamples;
import com.castle.domain.member.service.MemberService;
import com.castle.openai.service.OpenAiService;
import com.castle.response.BfResponse;
import com.castle.runpod.dto.request.CodeGenerateRequest;
import com.castle.runpod.service.RunPodStreamService;
import com.castle.security.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
@Tag(name = "AI 통신", description = "AI 통신 관련 API")
public class RunPodStreamController {

    private final RunPodStreamService runPodStreamService;
    private final OpenAiService openAiService;
    private final MemberService memberService;

    /**
     * 프론트엔드에서 prompt 파라미터를 받아 SSE 스트림으로 응답 전송
     */
    @Operation(
            summary = "ai 모델 코드 생성 요청",
            description = "ai 모델 코드 생성 요청합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코드 생성 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "FORBIDDEN_CHAT_SESSION_ACCESS"),
    })
    @PostMapping(value = "/generate-code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateCode(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody CodeGenerateRequest request) {
        System.out.println(request.getImageUrls());
        if (request.getImageUrls() != null) {
            String imagePrompt = openAiService.generateImagePrompt(request.getImageUrls());
            return runPodStreamService.streamToClient(
                    imagePrompt + "\n" + request.getPrompt(),
                    Long.parseLong(principalDetails.getUsername()),
                    request.getChatSessionId(), request.getImageUrls());
        }
        // gpt api로 이미지 변환 api 추가
        return runPodStreamService.streamToClient(request.getPrompt(), Long.parseLong(principalDetails.getUsername()), request.getChatSessionId(), null);
    }

    @Operation(
            summary = "llm 요청 취소",
            description = "llm 요청을 취소 합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 취소 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "FORBIDDEN_CHAT_SESSION_ACCESS"),
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "NOT_FOUND_CHAT_SESSION"),
    })
    @DeleteMapping(value = "/cancel/{chatSessionId}")
    public ResponseEntity<BfResponse<List<GlobalSuccessCode>>> stopRunPodSession(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                                                @PathVariable("chatSessionId") Long chatSessionId) {
        memberService.validateChatSessionAccess(Long.parseLong(principalDetails.getUsername()), chatSessionId);
        runPodStreamService.stopRunPodSession(chatSessionId);
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }

    /**
     * 프론트엔드에서 code prompt 파라미터를 받아 SSE 스트림으로 응답 전송
     */
    @Operation(
            summary = "ai 모델 코드 보안 리팩토링 요청",
            description = "ai 모델 코드 보안 리팩토링 요청합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코드 생성 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "FORBIDDEN_CHAT_SESSION_ACCESS"),
    })
    @PostMapping(value = "/refactor-code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter refactorCode(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody CodeGenerateRequest request) {
        return runPodStreamService.streamToClient(request.appendInstruction(), Long.parseLong(principalDetails.getUsername()), request.getChatSessionId(), null);
    }
}
