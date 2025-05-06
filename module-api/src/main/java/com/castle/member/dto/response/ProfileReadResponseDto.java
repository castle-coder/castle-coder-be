package com.castle.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 프로필 조회 응답 DTO 입니다.
 */
@Getter
@Builder
@Schema(description = "프로필 조회 응답 DTO 입니다.")
public class ProfileReadResponseDto {

    @Schema(description = "회원 ID")
    private Long id;

    @Schema(description = "회원 FirstName")
    private String firstName;

    @Schema(description = "회원 LastName")
    private String lastName;

    @Schema(description = "회원 이메일")
    private String email;

    @Schema(description = "회원 전화번호")
    private String phoneNumber;
}
