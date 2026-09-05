package com.tripping.backend.mypage.service;

import com.tripping.backend.entity.UserBadgeSetting;
import com.tripping.backend.mypage.dto.BadgeResponse;
import com.tripping.backend.mypage.repository.UserBadgeSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageBadgeService {

    // 꺼낼 뱃지(마이페이지 프로필에 노출) 최대 개수 - 마이페이지 프로필 줄 공간 제약 때문에 제한
    private static final int MAX_FEATURED_BADGES = 4;

    private final UserBadgeSettingRepository badgeSettingRepository;

    // 뱃지 목록 조회 - GET /users/me/badges
    // 유저가 한 번도 안 건드렸으면(행 없음) 카탈로그 앞에서부터 최대 MAX_FEATURED_BADGES개까지 featured=true로 최초 초기화
    @Transactional
    public List<BadgeResponse> getMyBadges(Long userId) {
        List<UserBadgeSetting> settings = badgeSettingRepository.findByUserId(userId);
        if (settings.isEmpty()) {
            BadgeCatalog[] catalog = BadgeCatalog.values();
            List<UserBadgeSetting> initialized = new java.util.ArrayList<>();
            for (int i = 0; i < catalog.length; i++) {
                initialized.add(badgeSettingRepository.save(
                        UserBadgeSetting.builder()
                                .userId(userId)
                                .badgeCode(catalog[i].name())
                                .featured(i < MAX_FEATURED_BADGES)
                                .build()
                ));
            }
            settings = initialized;
        }

        Map<String, Boolean> featuredByCode = settings.stream()
                .collect(Collectors.toMap(UserBadgeSetting::getBadgeCode, UserBadgeSetting::getFeatured));

        return java.util.Arrays.stream(BadgeCatalog.values())
                .map(badge -> new BadgeResponse(
                        badge.name(),
                        badge.label(),
                        badge.emoji(),
                        // 나중에 카탈로그가 늘어나서 아직 이 유저 행이 없는 뱃지면, 안전하게 기본 false
                        // (true로 두면 4개 제한을 몰래 넘길 수 있어서)
                        featuredByCode.getOrDefault(badge.name(), false)
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

        if (requestedFeatured.size() > MAX_FEATURED_BADGES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "꺼낼 뱃지는 최대 " + MAX_FEATURED_BADGES + "개까지 선택할 수 있습니다.");
        }

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
