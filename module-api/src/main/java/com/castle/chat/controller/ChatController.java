package com.castle.chat.controller;

import com.castle.code.error.MemberErrorCode;
import com.castle.code.success.GlobalSuccessCode;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.domain.chat.dto.request.ChatLogsReadResponse;
import com.castle.domain.chat.dto.request.ChatSessionsReadResponse;
import com.castle.domain.chat.service.ChatService;
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

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "LLM 채팅", description = "LLM 채팅 관련 API")
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "채팅 세션 READ",
            description = "사용자의 전체 채팅 세션을 생성일자(createdAt) 기준으로 내림차순 정렬해서 읽어오는 API입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "READ 성공")
    })
    @GetMapping("/sessions")
    public ResponseEntity<BfResponse<List<ChatSessionsReadResponse>>> readChatSessions(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(new BfResponse<>(chatService.getChatSessions(Long.parseLong(principalDetails.getUsername()))));
    }

    @Operation(
            summary = "채팅 로그 READ",
            description = "채팅 세션에 맞는 채팅 로그들을 읽어오는 API입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "READ 성공")
    })
    @GetMapping("/chat-logs")
    public ResponseEntity<BfResponse<List<ChatLogsReadResponse>>> readChatLogs(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam Long chatSessionId) {
        return ResponseEntity.ok(new BfResponse<>(chatService.getChatLogs(Long.parseLong(principalDetails.getUsername()), chatSessionId)));
    }

    @Operation(
            summary = "채팅 세션 추가",
            description = "사용자의 채팅 세션을 하나 추가합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "세션 추가 성공")
    })
    @PostMapping("/sessions")
    public ResponseEntity<BfResponse<Void>> appendChatSession(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        chatService.appendChatSession(Long.parseLong(principalDetails.getUsername()));
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }

    @Operation(
            summary = "채팅 세션 삭제",
            description = "사용자의 채팅 세션을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "세션 삭제 성공")
    })
    @DeleteMapping("/sessions")
    public ResponseEntity<BfResponse<Void>> appendChatSession(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam Long chatSessionId) {
        chatService.deleteChatSession(Long.parseLong(principalDetails.getUsername()), chatSessionId);
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }
}
