package com.castle.runpod.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CodeGenerateRequest {

    @Schema(description = "입력 프롬프트", example = "Please generate Python code for a bubble sort.", required = true)
    private final String prompt;
}
