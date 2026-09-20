package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.OrgApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgApiKeyRepository extends JpaRepository<OrgApiKey, Long> {

    List<OrgApiKey> findByOrgIdAndRevokedFalseOrderByCreatedAtDesc(Long orgId);

    Optional<OrgApiKey> findByKeyIdAndOrgId(Long keyId, Long orgId);
}
