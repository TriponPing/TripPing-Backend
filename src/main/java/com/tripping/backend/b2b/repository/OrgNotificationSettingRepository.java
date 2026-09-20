package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.OrgNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgNotificationSettingRepository extends JpaRepository<OrgNotificationSetting, Long> {

    Optional<OrgNotificationSetting> findByOrgId(Long orgId);
}
