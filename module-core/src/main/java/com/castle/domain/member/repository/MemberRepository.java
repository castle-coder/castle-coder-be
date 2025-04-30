package com.castle.domain.member.repository;

import com.castle.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository {
    // 회원 정보를 ID로 조회하는 메서드
    Optional<Member> findById(Long id);

    // 회원 정보를 이메일로 조회하는 메서드
    Optional<Member> findByEmail(String email);

    // 회원 정보를 ID와 역할로 조회하는 메서드
    Optional<Member> findByIdAndRoles(Long id, RolesType role);

    // 회원 정보를 이메일과 역할로 조회하는 메서드
    Optional<Member> findByEmailAndRoles(String email, RolesType role);
}
