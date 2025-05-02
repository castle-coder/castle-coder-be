package com.castle.security.provider;

import com.castle.security.service.jwt.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(CustomUserDetailsService userDetailsService,
                                        PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 사용자 식별자와 비밀번호 추출
        String email = authentication.getName();
        String rawPassword = (String) authentication.getCredentials();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // DB에서 사용자 정보 로드 (UserDetails 객체는 DB에 저장된 권한을 포함)
        UserDetails userDetails = userDetailsService.loadUserByUsernameAndRole(email, role);

        // 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }

        // 요청에서 전달된 권한과 DB의 권한 비교
        Collection<? extends GrantedAuthority> requestAuthorities = authentication.getAuthorities();
        Collection<? extends GrantedAuthority> dbAuthorities = userDetails.getAuthorities();

        for (GrantedAuthority reqAuth : requestAuthorities) {
            boolean hasAuthority = dbAuthorities.stream()
                    .anyMatch(dbAuth -> dbAuth.getAuthority().equals(reqAuth.getAuthority()));
            if (!hasAuthority) {
                throw new BadCredentialsException("요청한 권한(" + reqAuth.getAuthority() + ")이 사용자의 권한과 일치하지 않습니다.");
            }
        }

        // 검증이 완료되면 사용자 정보와 DB 권한으로 새 토큰 생성
        return new UsernamePasswordAuthenticationToken(userDetails, rawPassword, dbAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
