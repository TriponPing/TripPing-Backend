package com.tripping.backend.b2b.service;

import com.tripping.backend.b2b.dto.OrgResponse;
import com.tripping.backend.b2b.dto.OrgReviewRequestDto;
import com.tripping.backend.b2b.repository.OrganizationRepository;
import com.tripping.backend.entity.Organization;
import com.tripping.backend.entity.OrgStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class B2bAdminService {

    private final OrganizationRepository organizationRepository;

    public List<OrgResponse> pendingRequests() {
        return organizationRepository.findByStatusOrderByCreatedAtAsc(OrgStatus.PENDING)
                .stream()
                .map(OrgResponse::from)
                .toList();
    }

    public OrgResponse review(Long orgId, OrgReviewRequestDto request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청입니다: " + orgId));
        org.setStatus(request.getStatus());
        return OrgResponse.from(organizationRepository.save(org));
    }
}
