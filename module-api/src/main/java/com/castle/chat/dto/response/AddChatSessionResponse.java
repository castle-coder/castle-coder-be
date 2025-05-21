package com.castle.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class AddChatSessionResponse {

    @Schema(description = "추가된 세션 id", example = "1")
    private final Long chatSessionId;
}