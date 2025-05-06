package com.castle.domain.member.service;

import com.castle.code.error.MemberErrorCode;
import com.castle.domain.member.entity.Member;
import com.castle.domain.member.repository.MemberRepository;
import com.castle.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입을 위한 서비스 로직
     * @param member
     */
    @Transactional
    public void signUp(Member member) {
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new MemberException(MemberErrorCode.EXIST_MEMBER_EMAIL);
        }
        if (memberRepository.findByPhoneNumber(member.getPhoneNumber()).isPresent()) {
            throw new MemberException(MemberErrorCode.EXIST_PHONE_NUMBER);
        }
        memberRepository.save(member);
    }

    /**
     * 회원 정보를 ID로 조회하는 서비스 로직
     * @param memberId 조회하고자 하는 회원의 id
     * @return 조회된 회원 정보
     */
    @Transactional(readOnly = true)
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));
    }

    /**
     * 이메일 중복 체크를 위한 서비스 로직
     * @param email 중복 체크하고자 하는 이메일
     */
    @Transactional(readOnly = true)
    public void isEmailDuplicate(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new MemberException(MemberErrorCode.EXIST_MEMBER_EMAIL);
        }
    }

    /**
     * 회원 탈퇴를 위한 서비스 로직
     * @param memberId 탈퇴하고자 하는 회원의 id
     */
    @Transactional
    public void withdraw(Long memberId){
        // DB 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));
        // 로그인 회원탈퇴
        member.updateIsActive(false);
    }
}
