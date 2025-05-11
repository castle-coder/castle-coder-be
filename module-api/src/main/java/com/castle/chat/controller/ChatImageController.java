package com.castle.chat.controller;

import com.castle.aws.s3.service.ImageUrlService;
import com.castle.chat.dto.response.ImageUrlResponse;
import com.castle.code.error.ChatErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.code.error.S3ErrorCode;
import com.castle.code.success.GlobalSuccessCode;
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

@RestController
@RequestMapping("/chat/image")
@RequiredArgsConstructor
@Tag(name = "이미지 파일 채팅 입력", description = "이미지 파일 채팅 관련 API")
public class ChatImageController {

    private final ImageUrlService imageUrlService;

    @Operation(
            summary = "S3 Bucket에 채팅에 입력된 사진 업로드",
            description = "S3 Bucket에 채팅에 입력된 사진을 업로드합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "S3 사진 업로드 성공", useReturnTypeSchema = true),
    })
    @ApiErrorCodeExample(errorClass = S3ErrorCode.class, field = "INVALID_IMAGE_EXTENSION")
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BfResponse<ImageUrlResponse>> uploadChatImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "채팅에 입력된 사진입니다.")
            @RequestPart("updateImage") MultipartFile updateImage
    ) {
        ImageUrlResponse imageUrl = ImageUrlResponse.builder()
                .imageUrl(imageUrlService.uploadImage(updateImage, Long.parseLong(principalDetails.getUsername())))
                .build();
        return ResponseEntity.ok(new BfResponse<>(imageUrl));
    }

    @Operation(
            summary = "S3 Bucket에 채팅에 입력된 사진 삭제",
            description = "S3 Bucket에 채팅에 입력된 사진을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "S3 사진 삭제 성공", useReturnTypeSchema = true),
    })
    @DeleteMapping(value = "/")
    public ResponseEntity<BfResponse<Void>> deleteChatImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Parameter(description = "사진 URL입니다.", example = "https://.../ecc84...203.png")
            @RequestParam("imageUrl") String imageUrl
    ) {
        imageUrlService.deleteImage(imageUrl, Long.parseLong(principalDetails.getUsername()));
        return ResponseEntity.ok(new BfResponse<>(GlobalSuccessCode.SUCCESS));
    }
}
