package com.tripping.backend.auth.repository;

import com.tripping.backend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    // 이메일을 기준으로 회원을 찾아오는 쿼리 메서드 (로그인 시 사용)
    Optional<AppUser> findByEmail(String email);

    // 해당 이메일로 가입된 유저가 이미 존재하는지 true/false로 반환 (회원가입 중복 체크 시 사용)
    boolean existsByEmail(String email);
}