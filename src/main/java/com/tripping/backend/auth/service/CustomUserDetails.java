package com.tripping.backend.auth.service;

import com.tripping.backend.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


//Spring Security가 인증 과정에서 유저의 정보를 이해할 수 있도록,
//우리 앱의 AppUser 엔티티를 Spring Security 전용 UserDetails로 감싸주는(Adapter 패턴) 클래스입니다.


@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final AppUser appUser; // 실제 우리 유저 엔티티

    // 1. 유저가 가지고 있는 권한(Role) 목록을 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(appUser.getRole())); // 예: ROLE_USER
    }

    // 2. 유저의 암호화된 비밀번호 반환
    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    // 3. 유저를 식별할 수 있는 아이디(여기서는 email) 반환
    @Override
    public String getUsername() {
        return appUser.getEmail();
    }

    // 추가로 엔티티 고유의 PK(userId)나 닉네임을 꺼내 쓸 수 있도록 커스텀 메서드 제공
    public Long getUserId() {
        return appUser.getUserId();
    }

    public String getNickname() {
        return appUser.getNickname();
    }

    // 계정 만료 여부 (true: 만료 안 됨)
    @Override public boolean isAccountNonExpired() { return true; }
    // 계정 잠김 여부 (true: 잠기지 않음)
    @Override public boolean isAccountNonLocked() { return true; }
    // 비밀번호 만료 여부 (true: 만료 안 됨)
    @Override public boolean isCredentialsNonExpired() { return true; }
    // 계정 활성화 여부 (true: 사용 가능)
    @Override public boolean isEnabled() { return true; }
}