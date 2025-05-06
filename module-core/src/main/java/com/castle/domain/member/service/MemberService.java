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

    @Transactional(readOnly = true)
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));
    }

    @Transactional(readOnly = true)
    public void isEmailDuplicate(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new MemberException(MemberErrorCode.EXIST_MEMBER_EMAIL);
        }
    }
}
