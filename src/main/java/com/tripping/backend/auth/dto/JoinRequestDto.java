package com.tripping.backend.auth.dto;

import lombok.Getter;

@Getter // Lombok: JSON 데이터를 객체로 바꾼 뒤 값을 꺼내 쓸 수 있도록 각 필드의 Getter 자동 생성
public class JoinRequestDto {
    private String email; // 사용자가 입력한 이메일 (아이디로 사용)
    private String password; // 사용자가 입력한 비밀번호
    private String nickname; // 사용자가 입력한 닉네임
    private String regionId; // 회원가입 시 선택한 거주 지역 (Region.regionId, 예: "R01")
}
