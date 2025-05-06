package com.castle.security.config;

import com.castle.security.service.jwt.JwtService;
import io.jsonwebtoken.Claims;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Configuration
public class GrpcSecurityConfig {

    @Bean
    public GrpcAuthenticationReader grpcAuthenticationReader(JwtService jwtService) {
        // 간단한 예시: 토큰 문자열을 Authentication 객체로 매핑
        return new BearerAuthenticationReader(token -> {
            // 여기에 실제 JWT 검증 로직이 들어가야 함
             Claims claims = jwtService.getClaims(token);
                 return new UsernamePasswordAuthenticationToken(
                         claims.getSubject(),
                         null,
                         Collections.singletonList(new SimpleGrantedAuthority(claims.get("authorities").toString())));
        });
    }
}
