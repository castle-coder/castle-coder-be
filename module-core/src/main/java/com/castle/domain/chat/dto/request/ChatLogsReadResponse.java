package com.castle.domain.chat.dto.request;

import com.castle.domain.chat.entity.ChatLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatLogsReadResponse {

    @Schema(description = "chat ID")
    private Long id;

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "프롬프트")
    private String prompt;

    @Schema(description = "LLM 응답")
    private String response;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    public static ChatLogsReadResponse fromEntity(ChatLog chatLog) {
        return ChatLogsReadResponse.builder()
                .id(chatLog.getId())
                .memberId(chatLog.getMember().getId())
                .prompt(chatLog.getPrompt())
                .response(chatLog.getResponse())
                .createdAt(chatLog.getCreatedAt())
                .build();
    }
}
