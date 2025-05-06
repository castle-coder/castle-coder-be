package com.castle.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 회원 정보를 관리하는 엔티티 클래스입니다.
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email; // 이메일

    @Column(name = "password", nullable = false)
    private String password; // 비밀번호

    @Column(name = "first_name")
    private String firstName; // 사용자 성

    @Column(name = "last_name")
    private String lastName; // 사용자 성

    @Column(name = "phoneNumber")
    private String phoneNumber; // 전화번호

    @Column(name = "roles", nullable = false)
    @Enumerated(EnumType.STRING)
    private RolesType roles; // 권한

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 활성화 여부

    public void updateIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
