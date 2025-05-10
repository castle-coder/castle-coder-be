package com.castle.chat.controller;

import com.castle.chat.dto.response.ImageUrlResponse;
import com.castle.code.error.ChatErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.code.error.S3ErrorCode;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.documentation.annotation.ApiErrorCodeExamples;
import com.castle.domain.chat.dto.request.ChatLogsReadResponse;
import com.castle.domain.chat.service.ChatService;
import com.castle.response.BfResponse;
import com.castle.security.principal.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chat/image")
@RequiredArgsConstructor
@Tag(name = "이미지 파일 채팅", description = "이미지 파일 채팅 관련 API")
public class ChatImageController {

    private final ChatService chatService;

    @Operation(
            summary = "S3 Bucket에 가게 사진 업로드",
            description = "S3 Bucket에 가게의 사진을 업로드합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "S3 가게 사진 업로드 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExample(errorClass = S3ErrorCode.class, field = "INVALID_IMAGE_EXTENSION")
    @PostMapping(value = "/images/{marketId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BfResponse<ImageUrlResponse>> uploadMarketImage(
            @Parameter(description = "가게 사진입니다.")
            @RequestPart("updateImage") MultipartFile updateImage,
            @Parameter(description = "등록하려는 가게의 ID 입니다.") @PathVariable("marketId") Long marketId
    ) {
        return ResponseEntity.ok(new BfResponse<>());
    }
}
