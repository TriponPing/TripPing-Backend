package com.tripping.backend.b2b.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// null인 항목은 바꾸지 않는다. 이메일은 로그인 아이디라 여기서 바꾸지 않는다.
@Getter
@Setter
@NoArgsConstructor
public class OrgProfileUpdateRequest {
    private String orgName;
    private String orgType;
    private String managerName;
    private String department;
    private String description;
    private String logoUrl;
}
