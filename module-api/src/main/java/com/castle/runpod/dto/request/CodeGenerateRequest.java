package com.castle.runpod.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CodeGenerateRequest {

    @Schema(description = "채팅 세션 ID", example = "3", required = true)
    private final Long chatSessionId;

    @Schema(description = "입력 이미지 url", example = "\"[\"https://.../ab123...456.png\", \"https://.../cd123...456.png\"]\"")
    private final List<String> imageUrls;

    @Schema(description = "입력 프롬프트", example = "Please generate Python code for a bubble sort.")
    private final String prompt;

    public String appendInstruction() {
        return prompt.trim() + "\n" + "Divide the response into the complete code and a detailed explanation.";
    }
}
