package com.tripping.backend.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// 스프링 시큐리티 전체 보안 정책과 규칙을 설정하는 핵심 클래스입니다.


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 로그인이나 회원가입처럼, 로그인을 안 한 상태에서도 접근할 수 있어야 하는 URL 목록
    private static final String[] ALLOW_LIST = {
            "/auth/join", "/auth/login",
            "/places/**", "/map/places/**",   // 테스트용으로 임시 추가
            "/routes/map/search",
            "/regions" // 회원가입(사는 지역 선택) 화면은 로그인 전이라 전체 지역 목록 조회는 열어둬야 함
    };

    // permitAll 경로에서 에러(400/401 등)가 나면 스프링이 내부적으로 /error 로 다시 요청을 보내는데,
    // /error 자체가 인증을 요구하면 원래 에러 대신 403이 떠서 진짜 원인이 가려짐. 그래서 /error는
    // 요청 종류(dispatcher type)와 상관없이 항상 허용해야 함.
    private static final String[] ERROR_PATH = { "/error" };

    // 1. 비밀번호를 암호화할 때 사용할 BCrypt 인코더 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 로그인 처리를 위한 AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 3. 보안 필터 체인 설정 (어떤 요청은 허용하고, 어떤 요청은 막을지 결정)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // REST API + 모바일 앱 환경이라 CSRF 보안은 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 아래 정의한 CORS 설정 적용
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // 세션이 필요할 때만 생성
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ERROR_PATH).permitAll() // /error 내부 재전송은 항상 허용 (진짜 에러 코드가 403에 가려지는 것 방지)
                        .requestMatchers(ALLOW_LIST).permitAll() // ALLOW_LIST에 있는 주소는 로그인 없이 누구나 접근 가능
                        .requestMatchers("/swagger-ui/**").permitAll()// Swagger 문서 접근 허용
                        .requestMatchers("/v3/api-docs/**").permitAll() // Swagger API Docs 허용
                        // 👈 새로 추가: 업로드된 이미지는 조회(GET)만 로그인 없이 열어둠 - Coil 등에서
                        // 이미지 <img> 요청처럼 매번 세션 쿠키를 기대하기 어려워서. 업로드(POST)는
                        // 아래 anyRequest().authenticated()에 걸려 그대로 로그인 필요함.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/uploads/**").permitAll()
                        .anyRequest().authenticated() // 그 외의 모든 요청은 반드시 로그인이 필요함
                )
                .formLogin(form -> form.disable()) // 기본 제공되는 HTML 폼 로그인 화면 비활성화 (우리가 직접만든 AuthController 사용)
                .httpBasic(basic -> basic.disable()); // HTTP Basic 인증 비활성화

        return http.build();
    }

    // 4. CORS(Cross-Origin) 설정 (프론트엔드와 백엔드 포트가 다를 때 통신을 허용하기 위함)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // 허용할 도메인 (개발 중엔 전체 허용, 배포 땐 프론트 도메인만 넣는 것이 좋음)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드
        config.setAllowedHeaders(List.of("*")); // 허용할 헤더
        config.setAllowCredentials(true); // ★ 중요: 세션 쿠키를 프론트/앱과 주고받기 위해 반드시 true여야 함

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 이 CORS 정책 적용
        return source;
    }
}