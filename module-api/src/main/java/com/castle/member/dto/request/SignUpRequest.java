package com.castle.member.dto.request;

import com.castle.domain.member.entity.Member;
import com.castle.domain.member.entity.RolesType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 회원가입 요청에 필요한 정보를 담는 DTO 클래스입니다.
 */

@Getter
@RequiredArgsConstructor
public class SignUpRequest {

    @Schema(description = "이메일", example = "example12@gmail.com", required = true)
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
    private final String email;

    @Schema(description = "비밀번호(8글자 이상)", example = "example1234", required = true)
    @Size(min = 8, message = "비밀번호가 너무 짧습니다(8글자 이상)")
    private final String password;

    @Schema(description = "First Name", example = "길동", required = true)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{1,10}$", message = "First Name은 특수문자를 제외한 1~10자리여야 합니다.")
    private final String firstName;

    @Schema(description = "Last Name", example = "홍", required = true)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{1,10}$", message = "Last Name은 특수문자를 제외한 1~10자리여야 합니다.")
    private final String lastName;

    @Schema(description = "전화번호", example = "01011111111", required = true)
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10자리 또는 11자리 숫자여야 합니다.") // 전화번호 검증
    private final String phoneNumber;

    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .roles(RolesType.ROLE_USER)
                .isActive(true) // 기본값으로 활성화 상태로 설정
                .build();
    }
}
