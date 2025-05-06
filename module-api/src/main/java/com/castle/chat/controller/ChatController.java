package com.castle.chat.controller;

import com.castle.code.error.MemberErrorCode;
import com.castle.code.success.GlobalSuccessCode;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.member.dto.request.SignUpRequest;
import com.castle.response.BfResponse;
import com.castle.security.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "LLM 채팅", description = "LLM 채팅 관련 API")
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "채팅 세션 READ",
            description = "사용자의 전체 채팅 세션을 읽어오는 API입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "READ 성공")
    })
    @ApiErrorCodeExample(errorClass = MemberErrorCode.class, field = "EXIST_MEMBER_EMAIL")
    @GetMapping("/sessions")
    public ResponseEntity<BfResponse<Void>> readChatSessions(@AuthenticationPrincipal PrincipalDetails principalDetails) {

        chatService.readChatSessions(Long.parseLong(principalDetails.getUsername()));
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }
}
