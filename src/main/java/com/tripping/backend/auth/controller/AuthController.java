package com.tripping.backend.auth.controller;

import com.tripping.backend.auth.dto.JoinRequestDto;
import com.tripping.backend.auth.dto.LoginRequestDto;
import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.auth.service.JoinService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


//프론트엔드/앱과 통신하는 API 진입점(Controller) 입니다.


@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JoinService joinService;
    private final AuthenticationManager authenticationManager; // 로그인 검증을 담당하는 매니저

    // [회원가입 API] POST /api/auth/join
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody JoinRequestDto request) {
        joinService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 완료");
    }

    // [로그인 API] POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request,
                                        HttpServletRequest httpRequest) {
        // 1. 사용자가 입력한 이메일과 비밀번호로 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. AuthenticationManager가 CustomUserDetailsService와 PasswordEncoder를 호출해 검증 진행
        // (틀리면 여기서 예외가 터지거나 인증 실패 처리됨)
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 3. SecurityContext에 인증된 정보(Authentication) 저장
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 4. HTTP 세션을 생성하고, 세션 안에 시큐리티 Context를 통째로 저장 (이후 쿠키로 유지됨)
        httpRequest.getSession(true)
                .setAttribute("SPRING_SECURITY_CONTEXT", context);

        return ResponseEntity.ok("로그인 성공");
    }

    // [로그아웃 API] POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        request.getSession().invalidate(); // 세션 파기
        SecurityContextHolder.clearContext(); // 시큐리티 Context 비우기
        return ResponseEntity.ok("로그아웃 완료");
    }

    // [내 정보(로그인 상태) 확인 API] GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인하지 않았다면 userDetails가 null로 들어옴
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        // 로그인한 유저의 정보(ID, 이메일, 닉네임)를 JSON 형태로 반환
        return ResponseEntity.ok(Map.of(
                "userId", userDetails.getUserId(),
                "email", userDetails.getUsername(),
                "nickname", userDetails.getNickname()
        ));
    }
}