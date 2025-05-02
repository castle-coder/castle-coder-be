package com.castle.security.principal;

import com.castle.code.error.MemberErrorCode;
import com.castle.domain.member.entity.Member;
import com.castle.domain.member.entity.RolesType;
import com.castle.domain.member.repository.MemberRepository;
import com.castle.exception.MemberException;
import com.castle.security.service.jwt.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security의 UserDetailsService를 구현하여 사용자 정보를 조회하고,
 * 인증에 필요한 PrincipalDetails 객체를 반환하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements CustomUserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 기본 로그인 필터에서 id 또는 이메일로 회원정보를 찾기 위한 메서드
     * @param identifier 사용자 ID 또는 이메일
     * @return PrincipalDetails 객체
     * @throws UsernameNotFoundException 사용자 정보를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Member member;

        // 입력값이 숫자인지 확인 (ID 조회)
        if (isNumeric(identifier)) {
            member = memberRepository.findById(Long.valueOf(identifier))
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));
        } else {
            // 이메일 조회
            member = memberRepository.findByEmail(identifier)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));
        }
        return new PrincipalDetails(member);
    }

    /**
     * 기본 로그인 필터에서 id와 role로 회원정보를 찾기 위한 메서드
     * @param identifier 사용자 ID 또는 이메일
     * @param role 사용자 역할
     * @return PrincipalDetails 객체
     * @throws UsernameNotFoundException 사용자 정보를 찾을 수 없는 경우
     */
    @Transactional
    public UserDetails loadUserByUsernameAndRole(String identifier, String role) throws UsernameNotFoundException {
        Member member;

        // 입력값이 숫자인지 확인 (ID 조회)
        if (isNumeric(identifier)) {
            member = memberRepository.findByIdAndRoles(Long.valueOf(identifier), RolesType.valueOf(role))
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));
        } else {
            // 이메일 조회
            member = memberRepository.findByEmailAndRoles(identifier, RolesType.valueOf(role))
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));
        }

        return new PrincipalDetails(member);
    }

    /**
     * basic 로그인 필터에서 id와 role 로 회원정보를 찾기위한 메서드
     */
    public Member loadUserByIdAndRole(String id, String role) throws UsernameNotFoundException {
        Member member;

        member = memberRepository.findByIdAndRoles(Long.valueOf(id), RolesType.valueOf(role))
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));
        return member;
    }

    /**
     * 문자열이 숫자인지 확인하는 메서드
     */
    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
