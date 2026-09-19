package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.Organization;
import com.tripping.backend.entity.OrgStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByManagerEmail(String managerEmail);

    boolean existsByManagerEmail(String managerEmail);

    List<Organization> findByStatusOrderByCreatedAtAsc(OrgStatus status);
}
