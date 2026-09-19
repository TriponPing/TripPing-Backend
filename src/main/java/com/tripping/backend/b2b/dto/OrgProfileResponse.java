package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.Organization;
import lombok.Builder;
import lombok.Getter;

// 조직 설정 화면이 쓰는 기관 정보. 비밀번호는 절대 내려보내지 않는다.
@Getter
@Builder
public class OrgProfileResponse {
    private Long orgId;
    private String orgName;
    private String orgType;
    private String managerName;
    private String managerEmail;
    private String department;
    private String description;
    private String logoUrl;
    private String status;

    public static OrgProfileResponse from(Organization org) {
        return OrgProfileResponse.builder()
                .orgId(org.getOrgId())
                .orgName(org.getOrgName())
                .orgType(org.getOrgType())
                .managerName(org.getManagerName())
                .managerEmail(org.getManagerEmail())
                .department(org.getDepartment())
                .description(org.getDescription())
                .logoUrl(org.getLogoUrl())
                .status(org.getStatus().name())
                .build();
    }
}
