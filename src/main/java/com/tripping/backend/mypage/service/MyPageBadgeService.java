package com.tripping.backend.mypage.service;

import com.tripping.backend.entity.UserBadgeSetting;
import com.tripping.backend.mypage.dto.BadgeResponse;
import com.tripping.backend.mypage.repository.UserBadgeSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageBadgeService {

    private final UserBadgeSettingRepository badgeSettingRepository;

    // 뱃지 목록 조회 - GET /users/me/badges
    // 유저가 한 번도 안 건드렸으면(행 없음) 카탈로그 전부 featured=true로 최초 초기화
    @Transactional
    public List<BadgeResponse> getMyBadges(Long userId) {
        List<UserBadgeSetting> settings = badgeSettingRepository.findByUserId(userId);
        if (settings.isEmpty()) {
            settings = java.util.Arrays.stream(BadgeCatalog.values())
                    .map(badge -> badgeSettingRepository.save(
                            UserBadgeSetting.builder()
                                    .userId(userId)
                                    .badgeCode(badge.name())
                                    .featured(true)
                                    .build()
                    ))
                    .toList();
        }

        Map<String, Boolean> featuredByCode = settings.stream()
                .collect(Collectors.toMap(UserBadgeSetting::getBadgeCode, UserBadgeSetting::getFeatured));

        return java.util.Arrays.stream(BadgeCatalog.values())
                .map(badge -> new BadgeResponse(
                        badge.name(),
                        badge.label(),
                        badge.emoji(),
                        featuredByCode.getOrDefault(badge.name(), true)
                ))
                .toList();
    }

    // 꺼낼 뱃지 수정 - PUT /users/me/badges/featured
    @Transactional
    public List<BadgeResponse> updateFeaturedBadges(Long userId, List<String> featuredBadgeCodes) {
        Set<String> validCodes = java.util.Arrays.stream(BadgeCatalog.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        Set<String> requestedFeatured = featuredBadgeCodes == null
                ? Set.of()
                : featuredBadgeCodes.stream().filter(validCodes::contains).collect(Collectors.toSet());

        List<UserBadgeSetting> settings = badgeSettingRepository.findByUserId(userId);
        Map<String, UserBadgeSetting> settingByCode = settings.stream()
                .collect(Collectors.toMap(UserBadgeSetting::getBadgeCode, s -> s));

        for (BadgeCatalog badge : BadgeCatalog.values()) {
            boolean shouldFeature = requestedFeatured.contains(badge.name());
            UserBadgeSetting existing = settingByCode.get(badge.name());
            if (existing != null) {
                existing.setFeatured(shouldFeature);
            } else {
                badgeSettingRepository.save(
                        UserBadgeSetting.builder()
                                .userId(userId)
                                .badgeCode(badge.name())
                                .featured(shouldFeature)
                                .build()
                );
            }
        }

        return getMyBadges(userId);
    }
}
