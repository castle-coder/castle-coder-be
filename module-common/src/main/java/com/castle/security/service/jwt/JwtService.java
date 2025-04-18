package com.castle.security.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성, 검증, 헤더 설정, 권한 비교 등의 JWT 관련 기능을 제공하는 서비스 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration-seconds}")
    private long accessExpirationSeconds;

    @Value("${jwt.refresh-expiration-seconds}")
    private long refreshExpirationSeconds;

    private final PrincipalDetailsService principalDetailsService;

    /**
     * Access 토큰 생성
     */
    public String createAccessToken(MemberJwtDto memberJwtDto) {
        return createToken(memberJwtDto, accessExpirationSeconds);
    }

    /**
     * Refresh 토큰 생성
     */
    public String createRefreshToken(MemberJwtDto memberJwtDto) {
        return createToken(memberJwtDto, refreshExpirationSeconds);
    }

    /**
     * JWT 토큰 생성 공통 로직
     */
    private String createToken(MemberJwtDto memberJwtDto, long expirationTime) {
        // 토큰 생성에 사용할 변수들 정의
        String memberId = String.valueOf(memberJwtDto.getId());
        Date issuedAt = Date.from(Instant.now());
        Date expiration = Date.from(Instant.now().plusSeconds(expirationTime));
        String roles = (memberJwtDto.getAuthorities() == null || memberJwtDto.getAuthorities().isEmpty()) ? "" :
                memberJwtDto.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

        // JWT 토큰 생성
        return Jwts.builder()
                .setSubject(memberId)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .claim("roles", roles)
                .signWith(extractSecretKey(), SignatureAlgorithm.HS512) // 서명에 사용할 비밀 키
                .compact(); // JWT 문자열로 생성
    }

    /**
     * JWT 토큰을 기반으로 사용자 인증 객체를 생성
     */
    public Authentication createAuthentication(String token) {
        // JWT 토큰을 파싱하고 클레임(Claims) 추출
        Claims claims = getClaims(token);

        // ID 추출
        Long memberId = Long.valueOf(claims.getSubject());

        // 권한 정보 추출
        String roles = claims.get("roles", String.class);
        List<GrantedAuthority> authorities = (roles != null && !roles.trim().isEmpty()) ?
                List.of(new SimpleGrantedAuthority(roles)) : List.of();

        // 사용자 정보를 담은 UserDetails 객체 생성
        UserDetails userDetails = new PrincipalDetails(memberId, authorities);

        // Authentication 객체 생성하여 반환
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * 토큰 유효성 검증
     */
    public void validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(extractSecretKey())
                .build()
                .parseClaimsJws(token);
    }

    /**
     * 권한 비교
     */
    public void compareAuthorities(Authentication authentication) throws AccessDeniedException {
        // JWT 토큰의 권한
        Set<String> tokenAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 데이터베이스에 저장된 사용자 권한
        UserDetails userDetails = principalDetailsService.loadUserByUsername(authentication.getName());
        Set<String> dbAuthorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 권한 비교
        if (!tokenAuthorities.containsAll(dbAuthorities)) {
            throw new AccessDeniedException("JWT 토큰의 권한과 데이터베이스 권한이 일치하지 않습니다.");
        }
    }

    /**
     * JWT 토큰을 파싱하고 클레임(Claims) 추출
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(extractSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * SecretKey 추출
     */
    public SecretKey extractSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}