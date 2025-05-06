package com.castle.security.service.jwt;

import com.castle.code.error.JwtErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.domain.member.dto.MemberJwtDto;
import com.castle.domain.member.entity.Member;
import com.castle.domain.member.repository.MemberRepository;
import com.castle.exception.JwtException;
import com.castle.exception.MemberException;
import com.castle.security.dto.request.RefreshTokenRequest;
import com.castle.security.dto.response.JwtTokenResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JWT 토큰 생성을 관리하는 서비스 클래스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    /**
     * 기존 Refresh token 으로 신규 Access token 및 Refresh token 발급
     */
    @Transactional
    public JwtTokenResponse refreshTokens(RefreshTokenRequest refreshTokenRequest) {
        // 유효성 검증
        try {
            jwtService.validateToken(refreshTokenRequest.getRefreshToken());
        } catch (Exception e) {
            throw new JwtException(JwtErrorCode.INVALID_TOKEN);
        }

        // ID로 회원 조회
        Claims claims = jwtService.getClaims(refreshTokenRequest.getRefreshToken());
        Long memberId = Long.valueOf(claims.getSubject());

        // DB 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));

        // MemberJwtDto 변환
        MemberJwtDto memberJwtDto = new MemberJwtDto(member);

        return createAccessTokenAndRefreshToken(memberJwtDto);
    }

    /**
     * Access Token 및 Refresh Token 발급
     */
    private JwtTokenResponse createAccessTokenAndRefreshToken(MemberJwtDto memberJwtDto) {
        String accessToken = jwtService.createAccessToken(memberJwtDto);
        String refreshToken = jwtService.createRefreshToken(memberJwtDto);

        return JwtTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}