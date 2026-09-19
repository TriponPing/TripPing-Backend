package com.tripping.backend.b2b.dto;

import lombok.Getter;

@Getter
public class OrgLoginRequestDto {
    private String email;    // 기관 담당자 이메일 또는 운영자(Admin) 이메일 - 같은 로그인 창을 씀
    private String password;
}
