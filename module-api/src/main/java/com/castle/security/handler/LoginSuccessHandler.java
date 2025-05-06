package com.castle.security.handler;

import com.castle.domain.member.dto.MemberJwtDto;
import com.castle.domain.member.entity.Member;
import com.castle.response.BfResponse;
import com.castle.security.dto.response.JwtTokenResponse;
import com.castle.security.principal.PrincipalDetailsService;
import com.castle.security.service.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final PrincipalDetailsService principalDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 인증된 사용자 정보 가져오기
        String id = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElseThrow(() -> new RuntimeException("권한 정보가 없습니다."));
        Member member = principalDetailsService.loadUserByIdAndRole(id, role);  // Long 타입으로 변환

        // JWT 토큰 생성
        MemberJwtDto memberJwtDto = new MemberJwtDto(member);
        String accessToken = jwtService.createAccessToken(memberJwtDto);
        String refreshToken = jwtService.createRefreshToken(memberJwtDto);

        // 응답 데이터 생성
        JwtTokenResponse jwtTokenResponse = new JwtTokenResponse(accessToken, refreshToken);
        BfResponse<JwtTokenResponse> responseBody = new BfResponse<>(jwtTokenResponse);

        // 응답 설정 및 전송
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
