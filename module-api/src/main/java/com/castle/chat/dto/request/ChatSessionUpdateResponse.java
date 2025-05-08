package com.castle.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatSessionUpdateResponse {

    @Schema(description = "채팅 세션 ID", example = "3", required = true)
    private final Long chatSessionId;

    @Schema(description = "채팅 세션 TITLE", example = "변경할 채팅세션 제목", required = true)
    private final String title;

}