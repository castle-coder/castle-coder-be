package com.castle.domain.member.repository;

import com.castle.domain.member.entity.Member;
import com.castle.domain.member.entity.RolesType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 도메인의 Repository 입니다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 회원 정보를 이메일로 조회하는 메서드
    Optional<Member> findByEmail(String email);

    // 회원 정보를 전화번호로 조회하는 메서드
    Optional<Member> findByPhoneNumber(String phoneNumber);

    // 회원 정보를 ID와 역할로 조회하는 메서드
    Optional<Member> findByIdAndRoles(Long id, RolesType role);

    // 회원 정보를 이메일과 역할로 조회하는 메서드
    Optional<Member> findByEmailAndRoles(String email, RolesType role);
}
