package com.castle.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * S3 Bucket에 사진 업로드 요청에 대한 응답 데이터를 담는 DTO 클래스입니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class ImageUrlResponse {

    @Schema(description = "등록된 사진의 url", example = "https://.../ecc84...203.png")
    private final String imageUrl;
}