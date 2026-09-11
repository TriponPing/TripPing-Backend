package com.tripping.backend.mypage.service;

import com.tripping.backend.entity.RouteStatus;
import com.tripping.backend.entity.UserBadgeSetting;
import com.tripping.backend.mypage.dto.BadgeResponse;
import com.tripping.backend.mypage.repository.MyPageActualRouteRepository;
import com.tripping.backend.mypage.repository.MyPageActualRouteSpotRepository;
import com.tripping.backend.mypage.repository.MyPagePingLogRepository;
import com.tripping.backend.mypage.repository.MyPagePingLogTagRepository;
import com.tripping.backend.mypage.repository.MyPageRegionPingRepository;
import com.tripping.backend.mypage.repository.MyPageSavedPlaceRepository;
import com.tripping.backend.mypage.repository.MyPageSavedRouteRepository;
import com.tripping.backend.mypage.repository.UserBadgeSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
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

    // 뱃지 달성 조건 기준값 - BadgeCatalog의 conditionDesc 문구와 반드시 같이 맞춰서 바꿔야 함
    private static final int REGION_EXPLORER_MIN_REGIONS = 5;
    private static final int NATIONWIDE_EXPLORER_MIN_REGIONS = 10;
    private static final int ROUTE_VETERAN_MIN_TRIPS = 5;
    private static final int STORYTELLER_MIN_REVIEWS = 10;
    private static final int TAG_COLLECTOR_MIN_TAGS = 30;
    private static final int TREASURE_KEEPER_MIN_SAVED = 10;
    private static final int STAR_RATER_MIN_RATED = 20;

    private final UserBadgeSettingRepository badgeSettingRepository;
    private final MyPageActualRouteRepository actualRouteRepository;
    private final MyPageActualRouteSpotRepository actualRouteSpotRepository;
    private final MyPageRegionPingRepository regionPingRepository;
    private final MyPagePingLogRepository pingLogRepository;
    private final MyPagePingLogTagRepository pingLogTagRepository;
    private final MyPageSavedPlaceRepository savedPlaceRepository;
    private final MyPageSavedRouteRepository savedRouteRepository;

    // 뱃지 목록 조회 - GET /users/me/badges
    // 뱃지 달성 여부(earned)는 UserLevel과 같은 방식으로 저장하지 않고 매번 실제 활동 데이터로 계산함
    // (별도 갱신 로직/버그 걱정 없이 항상 실제 상태와 정확히 일치). featured(꺼내기 여부)만 DB에 저장.
    public List<BadgeResponse> getMyBadges(Long userId) {
        Map<BadgeCatalog, Boolean> earnedByBadge = computeEarnedStatus(userId);
        Map<String, Boolean> featuredByCode = badgeSettingRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserBadgeSetting::getBadgeCode, UserBadgeSetting::getFeatured));

        return java.util.Arrays.stream(BadgeCatalog.values())
                .map(badge -> new BadgeResponse(
                        badge.name(),
                        badge.label(),
                        badge.emoji(),
                        badge.conditionDesc(),
                        earnedByBadge.getOrDefault(badge, false),
                        featuredByCode.getOrDefault(badge.name(), false)
                ))
                .toList();
    }

    // 꺼낼 뱃지 수정 - PUT /users/me/badges/featured
    @Transactional
    public List<BadgeResponse> updateFeaturedBadges(Long userId, List<String> featuredBadgeCodes) {
        Map<BadgeCatalog, Boolean> earnedByBadge = computeEarnedStatus(userId);
        Set<String> earnedCodes = earnedByBadge.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(entry -> entry.getKey().name())
                .collect(Collectors.toSet());

        // 아직 달성하지 못한 뱃지는 꺼낼 수 없음 - 요청에 섞여 있어도 조용히 걸러냄
        Set<String> requestedFeatured = featuredBadgeCodes == null
                ? Set.of()
                : featuredBadgeCodes.stream().filter(earnedCodes::contains).collect(Collectors.toSet());

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
            } else if (shouldFeature) {
                badgeSettingRepository.save(
                        UserBadgeSetting.builder()
                                .userId(userId)
                                .badgeCode(badge.name())
                                .featured(true)
                                .build()
                );
            }
        }

        return getMyBadges(userId);
    }

    // 뱃지 10종 각각의 달성 조건을 실제 활동 데이터로 계산. BadgeCatalog에 새 뱃지가 추가되면
    // 여기에도 조건을 같이 추가해야 함(안 넣으면 그 뱃지는 항상 미달성으로 표시됨).
    private Map<BadgeCatalog, Boolean> computeEarnedStatus(Long userId) {
        Map<BadgeCatalog, Boolean> result = new EnumMap<>(BadgeCatalog.class);

        long completedTrips = actualRouteRepository.countByUserIdAndIsDeletedFalseAndStatus(userId, RouteStatus.COMPLETED);
        long totalPings = actualRouteSpotRepository.countByUserId(userId);
        long distinctRegions = actualRouteSpotRepository.countDistinctRegionsByUserId(userId);
        long regionPings = regionPingRepository.countByUserId(userId);
        long reviews = pingLogRepository.countReviewsByUserId(userId);
        long ratedPings = pingLogRepository.countRatedPingsByUserId(userId);
        long tags = pingLogTagRepository.countByUserId(userId);
        long savedCount = savedPlaceRepository.countByUserId(userId) + savedRouteRepository.countByUserId(userId);

        result.put(BadgeCatalog.FIRST_PING, totalPings >= 1);
        result.put(BadgeCatalog.ROUTE_FINISHER, completedTrips >= 1);
        result.put(BadgeCatalog.ROUTE_VETERAN, completedTrips >= ROUTE_VETERAN_MIN_TRIPS);
        result.put(BadgeCatalog.REGION_PING_STARTER, regionPings >= 1);
        result.put(BadgeCatalog.REGION_EXPLORER, distinctRegions >= REGION_EXPLORER_MIN_REGIONS);
        result.put(BadgeCatalog.NATIONWIDE_EXPLORER, distinctRegions >= NATIONWIDE_EXPLORER_MIN_REGIONS);
        result.put(BadgeCatalog.STORYTELLER, reviews >= STORYTELLER_MIN_REVIEWS);
        result.put(BadgeCatalog.TAG_COLLECTOR, tags >= TAG_COLLECTOR_MIN_TAGS);
        result.put(BadgeCatalog.TREASURE_KEEPER, savedCount >= TREASURE_KEEPER_MIN_SAVED);
        result.put(BadgeCatalog.STAR_RATER, ratedPings >= STAR_RATER_MIN_RATED);

        return result;
    }
}
