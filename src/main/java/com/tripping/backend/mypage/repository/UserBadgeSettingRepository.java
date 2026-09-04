package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.UserBadgeSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeSettingRepository extends JpaRepository<UserBadgeSetting, Long> {

    List<UserBadgeSetting> findByUserId(Long userId);

    Optional<UserBadgeSetting> findByUserIdAndBadgeCode(Long userId, String badgeCode);
}
