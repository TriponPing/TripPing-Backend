package com.tripping.backend.home.service;

import com.tripping.backend.auth.repository.UserRepository;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.home.dto.response.PopularTripResponse;
import com.tripping.backend.home.repository.HomeActualRouteRepository;
import com.tripping.backend.home.repository.HomeSavedRouteRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularTripService {

    private static final String UNKNOWN_NICKNAME = "알 수 없음";

    /** "이번주" 판단 기준 일수. period 값이 늘어나면(예: month) resolveWindowDays()만 확장하면 됩니다. */
    private static final int WEEK_WINDOW_DAYS = 7;

    private final HomeSavedRouteRepository savedRouteRepository;
    private final HomeActualRouteRepository actualRouteRepository;
    private final UserRepository userRepository; // auth 도메인의 AppUser Repository를 그대로 재사용합니다.

    public List<PopularTripResponse> getPopularTrips(String period, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(resolveWindowDays(period));
        Pageable topN = PageRequest.of(0, limit);

        List<Long> popularRouteIds = savedRouteRepository.findPopularRouteIds(since, topN);
        if (popularRouteIds.isEmpty()) {
            return List.of();
        }

        // findByActualRouteIdIn... 은 순서를 보장하지 않으므로 저장 수 내림차순(popularRouteIds 순서)대로 다시 정렬합니다.
        Map<Long, ActualRoute> routeById = new LinkedHashMap<>();
        actualRouteRepository.findByActualRouteIdInAndIsPublicTrueAndIsDeletedFalse(popularRouteIds)
                .forEach(route -> routeById.put(route.getActualRouteId(), route));

        return popularRouteIds.stream()
                .map(routeById::get)
                .filter(route -> route != null)
                .map(route -> PopularTripResponse.from(
                        route,
                        findNickname(route.getUserId()),
                        savedRouteRepository.countRecentSavesByRouteId(route.getActualRouteId(), since)))
                .toList();
    }

    /** 지금은 "week"만 지원합니다. 다른 period(예: month, all)가 필요해지면 여기만 확장하면 됩니다. */
    private int resolveWindowDays(String period) {
        return WEEK_WINDOW_DAYS;
    }

    private String findNickname(Long userId) {
        return userRepository.findById(userId)
                .map(AppUser::getNickname)
                .orElse(UNKNOWN_NICKNAME);
    }
}
