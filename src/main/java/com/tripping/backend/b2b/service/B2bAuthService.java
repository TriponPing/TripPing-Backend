package com.tripping.backend.b2b.service;

import com.tripping.backend.b2b.dto.OrgJoinRequestDto;
import com.tripping.backend.b2b.dto.OrgLoginRequestDto;
import com.tripping.backend.b2b.repository.AdminRepository;
import com.tripping.backend.b2b.repository.OrganizationRepository;
import com.tripping.backend.entity.Admin;
import com.tripping.backend.entity.Organization;
import com.tripping.backend.entity.OrgStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 기관(Organization) / 운영자(Admin) 전용 인증 서비스.
//
// 앱이 쓰는 AuthController/CustomUserDetailsService(AppUser 대상)와는 완전히 별개 경로다.
// 이 프로젝트는 앱(AppUser)과 B2B 웹 포털(Organization, Admin)이 같은 백엔드를 쓰지만,
// 로그인 대상 테이블 자체가 다르기 때문에 /auth/login을 재사용하지 않고
// /b2b/auth/login을 새로 둔다. AuthenticationManager를 그대로 쓰지 않고 여기서
// 비밀번호 검증과 SecurityContext 구성을 직접 하는 이유: UserDetailsService 빈을
// 두 개(CustomUserDetailsService, 이것) 두면 스프링 시큐리티의 기본
// AuthenticationManager 자동구성이 어느 쪽을 쓸지 애매해지는 문제를 피하기 위함.
@RequiredArgsConstructor
@Service
public class B2bAuthService {

    private final OrganizationRepository organizationRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public void join(OrgJoinRequestDto request) {
        if (organizationRepository.existsByManagerEmail(request.getManagerEmail())) {
            throw new IllegalArgumentException("이미 신청된 이메일입니다.");
        }
        Organization org = Organization.builder()
                .orgName(request.getOrgName())
                .orgType(request.getOrgType())
                .managerName(request.getManagerName())
                .managerEmail(request.getManagerEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .documentUrl(request.getDocumentUrl())
                .status(OrgStatus.PENDING)
                .build();
        organizationRepository.save(org);
    }

    public void login(OrgLoginRequestDto request, HttpServletRequest httpRequest) {
        Object principal;
        String authority;

        // 운영자(Admin) 테이블을 먼저 확인하고, 없으면 기관(Organization) 테이블을 확인한다.
        // 같은 로그인 폼(이메일+비밀번호)을 운영자/기관 양쪽이 공용으로 쓰기 때문.
        Admin admin = adminRepository.findByEmail(request.getEmail()).orElse(null);
        if (admin != null) {
            if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
            }
            principal = admin;
            authority = "ROLE_ADMIN";
        } else {
            Organization org = organizationRepository.findByManagerEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 이메일입니다: " + request.getEmail()));
            if (!passwordEncoder.matches(request.getPassword(), org.getPassword())) {
                throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
            }
            // 승인 대기(PENDING)/반려(REJECTED) 상태여도 로그인 자체는 허용한다 —
            // 프론트가 로그인 후 상태를 보고 "승인 대기 중입니다" 화면으로 보내는 구조라서.
            principal = org;
            authority = "ROLE_ORG";
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(authority)));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        httpRequest.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", context);
    }

    public Object me(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof Admin admin) {
            return Map.of(
                    "type", "admin",
                    "adminId", admin.getAdminId(),
                    "email", admin.getEmail()
            );
        }

        if (principal instanceof Organization org) {
            Map<String, Object> body = new HashMap<>();
            body.put("type", "org");
            body.put("orgId", org.getOrgId());
            body.put("orgName", org.getOrgName());
            body.put("orgType", org.getOrgType());
            body.put("managerName", org.getManagerName());
            body.put("managerEmail", org.getManagerEmail());
            body.put("status", org.getStatus().name());
            return body;
        }

        throw new IllegalStateException("알 수 없는 인증 주체입니다.");
    }
}
