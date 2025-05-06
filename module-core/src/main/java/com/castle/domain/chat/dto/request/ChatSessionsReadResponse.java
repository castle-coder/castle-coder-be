package com.castle.domain.chat.dto.request;

import com.castle.domain.chat.entity.ChatSession;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatSessionsReadResponse {
    private Long id;
    private Long memberId;
    private String title;
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
