package com.tripping.backend.auth.service;

import com.tripping.backend.auth.dto.JoinRequestDto;
import com.tripping.backend.auth.repository.UserRepository;
import com.tripping.backend.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

//회원가입 비즈니스 로직을 처리하는 서비스입니다.

@RequiredArgsConstructor
@Service
public class JoinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 도구

    public void join(JoinRequestDto request) {
        // 1. 이미 가입된 이메일인지 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 2. 유저 엔티티 생성 (비밀번호는 반드시 암호화해서 저장!)
        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt로 암호화
                .nickname(request.getNickname())
                .role("ROLE_USER") // 기본 권한 부여
                .build();

        // 3. DB에 저장
        userRepository.save(appUser);
    }
}