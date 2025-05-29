package com.castle.security.config;

import com.castle.security.filter.AuthenticatedJwtFilter;
import com.castle.security.filter.CustomUsernamePasswordAuthenticationFilter;
import com.castle.security.handler.CustomAccessDeniedHandler;
import com.castle.security.handler.LoginFailureHandler;
import com.castle.security.handler.LoginSuccessHandler;
import com.castle.security.principal.PrincipalDetailsService;
import com.castle.security.provider.CustomAuthenticationProvider;
import com.castle.security.service.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * JWT 관련 설정을 포함하고 있는 Spring Security 설정 클래스입니다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final ObjectMapper objectMapper;
    private final PrincipalDetailsService principalDetailsService;

    /**
     * 로그인 인증 작업 처리
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * CustomAuthenticationProvider Bean 등록
     */
    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(principalDetailsService, passwordEncoder());
    }

    /**
     * CustomUsernamePasswordAuthenticationFilter 빈 등록
     * 커스텀 필터를 사용하기 위해 만든 커스텀 필터를 Bean으로 등록
     * setAuthenticationManager(authenticationManager())로 위에서 등록한 AuthenticationManager(ProviderManager) 설정
     * 로그인 성공 시 호출할 handler, 실패 시 호출할 handler로 위에서 등록한 handler 설정
     */
    @Bean
    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        CustomUsernamePasswordAuthenticationFilter customFilter = new CustomUsernamePasswordAuthenticationFilter(objectMapper);
        customFilter.setAuthenticationManager(authenticationManager);
        customFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customFilter;
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtService, principalDetailsService, objectMapper);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler(objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 및 권한이 필요한 필터 체인
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authenticatedFilterChain(HttpSecurity http) throws Exception {
        defaultSecuritySetting(http);
        http
                .securityMatchers(matcher -> matcher
                        .requestMatchers(authenticatedRequestMatchers()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(authenticatedRequestMatchers())
                        .hasAnyAuthority("ROLE_USER") // role 추가 예정
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(new AuthenticatedJwtFilter(jwtService), ExceptionTranslationFilter.class);

        return http.build();
    }

    /**
     * 공개 접근 필터 체인. SecurityContext에 회원 정보를 저장할 필요가 없는 요청에 대한 필터
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));
        http.authenticationProvider(customAuthenticationProvider());
        CustomUsernamePasswordAuthenticationFilter authenticationFilter = customUsernamePasswordAuthenticationFilter(authenticationManager);
        defaultSecuritySetting(http);
        http
                .securityMatchers(matcher -> matcher
                        .requestMatchers(publicRequestMatchers()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(publicRequestMatchers()).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authenticationFilter, ExceptionTranslationFilter.class);;
        return http.build();
    }

    /**
     * 인증 및 권한이 필요한 endpoint
     */
    private RequestMatcher[] authenticatedRequestMatchers() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher(OPTIONS, "/**"),
                antMatcher(POST, "/chat/session"), // 채팅 세션 추가
                antMatcher(GET, "/chat/session"), // 채팅 세션 READ
                antMatcher(DELETE, "/chat/session"), // 채팅 세션 DELETE
                antMatcher(PATCH, "/chat/session"), // 채팅 세션 UPDATE
                antMatcher(GET, "/chat/logs"), // 채팅 로그 READ

                // SSE 엔드포인트
                antMatcher(POST, "/llm/generate-code"), // ai 모델 코드 생성 요청
                antMatcher(POST, "/llm/refactor-code"), // ai 모델 코드 리팩토링 요청
                antMatcher(DELETE, "/llm/cancel/[chatSessionId}"), // llm 요청 취소

                antMatcher(POST, "/chat/image"), // S3 Bucket에 채팅에 입력된 사진 업로드
                antMatcher(DELETE, "/chat/image"), // S3 Bucket에 채팅에 입력된 사진 삭제

                antMatcher(GET, "/member/profiles"), // 회원 프로필 조회
                antMatcher(DELETE, "/member/withdraw") // 회원 탈퇴
        );

        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    /**
     * 공개 접근 endpoint
     */
    private RequestMatcher[] publicRequestMatchers() {
        List<RequestMatcher> requestMatchers = List.of(
                antMatcher(OPTIONS, "/**"),
                // swagger
                antMatcher(GET, "/swagger-ui/**"), // Swagger UI 웹 인터페이스를 제공하는 경로
                antMatcher(GET, "/v3/api-docs/**"), // Swagger의 API 문서 데이터를 JSON 형식으로 제공하는 경로

                antMatcher(GET, "/utils/health"), // 헬스 체크

                antMatcher(POST, "/auth/login"), // 로그인

                antMatcher(POST, "/member/sign-up"), // 회원가입
                antMatcher(GET, "/member/check-email") // 이메일 중복확인
        );

        return requestMatchers.toArray(RequestMatcher[]::new);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 필요한 Origin 을 등록 ( * 로 두면 모든 Origin 허용 )
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://your-front.com"));
        // pre-flight 를 포함한 모든 메서드 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));        // 모든 요청 헤더 허용
        config.setExposedHeaders(List.of("Authorization")); // 응답에서 노출할 헤더
        config.setAllowCredentials(true);              // 쿠키/Authorization 헤더 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    /**
     * Spring Security 기본 설정 메서드
     */
    private void defaultSecuritySetting(HttpSecurity http) throws Exception {
        http
                // JWT, OAuth 기반
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable) // Form 기반 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .rememberMe(AbstractHttpConfigurer::disable) // 세션 기반의 인증 비활성화
                .logout(AbstractHttpConfigurer::disable) // 로그아웃 기능 비활성화
                .anonymous(AbstractHttpConfigurer::disable) // 익명 사용자 접근 비할성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 생성하지 않음
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(OPTIONS, "/**").permitAll() )  // ✅
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) // X-Frame-Options 헤더 비활성화
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self' vscode-webview://* http://13.125.85.38")
                        ) // CSP 정책 추가
                )
                // 비동기 요청에서 SecurityContext 전파 설정
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(false));
    }
}
