package com.tripping.backend.b2b.dto;

import lombok.Getter;

@Getter
public class OrgJoinRequestDto {
    private String orgName;       // 기관명
    private String orgType;       // 여행사/지자체 등
    private String managerName;   // 담당자 이름
    private String managerEmail;  // 담당자 이메일 (로그인 아이디로 사용)
    private String password;      // 로그인 비밀번호
    private String documentUrl;   // 재직/기관 증빙파일 URL (FileUploadController로 먼저 업로드한 뒤 받은 URL을 넣어줌)
}
