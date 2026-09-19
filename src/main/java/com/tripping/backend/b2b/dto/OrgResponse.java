package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.Organization;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrgResponse {
    private Long orgId;
    private String orgName;
    private String orgType;
    private String managerName;
    private String managerEmail;
    private String documentUrl;
    private String status;
    private LocalDateTime createdAt;

    public static OrgResponse from(Organization org) {
        return OrgResponse.builder()
                .orgId(org.getOrgId())
                .orgName(org.getOrgName())
                .orgType(org.getOrgType())
                .managerName(org.getManagerName())
                .managerEmail(org.getManagerEmail())
                .documentUrl(org.getDocumentUrl())
                .status(org.getStatus().name())
                .createdAt(org.getCreatedAt())
                .build();
    }
}
