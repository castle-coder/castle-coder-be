package com.castle.domain.chat.dto.request;

import com.castle.domain.chat.entity.ChatSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatSessionsReadResponse {

    @Schema(description = "chat session ID")
    private Long id;

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "chat session 제목")
    private String title;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    public static ChatSessionsReadResponse fromEntity(ChatSession session) {
        return ChatSessionsReadResponse.builder()
                .id(session.getId())
                .memberId(session.getMemberId())
                .title(session.getTitle())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
