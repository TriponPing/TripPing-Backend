package com.tripping.backend.auth.dto;

import lombok.Getter;

@Getter
public class LoginRequestDto {
    private String email; // 로그인할 이메일
    private String password; // 로그인할 비밀번호
}
