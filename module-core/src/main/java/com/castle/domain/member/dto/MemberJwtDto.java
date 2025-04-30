package com.castle.domain.member.dto;

import com.castle.domain.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

/**
 * JWT 토큰 생성을 위한 회원 정보를 담고 있는 DTO 클래스입니다.
 */
@Getter
public class MemberJwtDto {

    private final Long id;
    private final List<GrantedAuthority> authorities;

    public MemberJwtDto(Member member) {
        this.id = member.getId();
        this.authorities = Optional.ofNullable(member.getRoles())
                .map(role -> List.<GrantedAuthority>of(new SimpleGrantedAuthority(role.name())))
                .orElse(List.of());
    }
}