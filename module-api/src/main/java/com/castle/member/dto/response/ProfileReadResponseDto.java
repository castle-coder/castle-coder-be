package com.castle.member.dto.response;

import com.castle.domain.member.entity.Member;
import com.castle.domain.member.entity.RolesType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    public static ProfileReadResponseDto from(Member member) {
        return ProfileReadResponseDto.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .build();
    }
}
