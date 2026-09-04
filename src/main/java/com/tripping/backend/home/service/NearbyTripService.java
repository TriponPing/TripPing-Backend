package com.tripping.backend.home.service;

import com.tripping.backend.auth.repository.UserRepository;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.home.dto.response.NearbyTripResponse;
import com.tripping.backend.home.repository.HomeActualRouteRepository;
import com.tripping.backend.home.repository.HomeActualRouteSpotRepository;
import com.tripping.backend.home.repository.NearbyRouteProjection;
import com.tripping.backend.mypage.dto.PageResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NearbyTripService {

    private static final String UNKNOWN_NICKNAME = "알 수 없음";

    /** 기본 반경(km). 프론트에서 radiusKm 을 안 보내면 이 값을 씁니다. */
    public static final double DEFAULT_RADIUS_KM = 10.0;

    private final HomeActualRouteSpotRepository actualRouteSpotRepository;
    private final HomeActualRouteRepository actualRouteRepository;
    private final UserRepository userRepository; // auth 도메인의 AppUser Repository를 그대로 재사용합니다.

    public PageResponse<NearbyTripResponse> getNearbyTrips(
            double lat, double lng, Double radiusKm, int page, int size) {
        double effectiveRadiusKm = (radiusKm != null) ? radiusKm : DEFAULT_RADIUS_KM;
        Pageable pageable = PageRequest.of(page, size);

        Page<NearbyRouteProjection> nearbyPage =
                actualRouteSpotRepository.findNearbyRoutes(lat, lng, effectiveRadiusKm, pageable);

        List<Long> routeIds = nearbyPage.getContent().stream()
                .map(NearbyRouteProjection::getActualRouteId)
                .toList();

        if (routeIds.isEmpty()) {
            return PageResponse.of(List.of(), nearbyPage);
        }

        // findByActualRouteIdIn... 은 순서를 보장하지 않으므로 거리순(nearbyPage 순서)으로 다시 정렬합니다.
        Map<Long, ActualRoute> routeById = new LinkedHashMap<>();
        actualRouteRepository.findByActualRouteIdInAndIsPublicTrueAndIsDeletedFalse(routeIds)
                .forEach(route -> routeById.put(route.getActualRouteId(), route));

        List<NearbyTripResponse> content = nearbyPage.getContent().stream()
                .map(projection -> {
                    ActualRoute route = routeById.get(projection.getActualRouteId());
                    if (route == null) {
                        return null;
                    }
                    String nickname = findNickname(route.getUserId());
                    return NearbyTripResponse.from(route, nickname, projection.getDistanceKm());
                })
                .filter(response -> response != null)
                .toList();

        return PageResponse.of(content, nearbyPage);
    }

    private String findNickname(Long userId) {
        return userRepository.findById(userId)
                .map(AppUser::getNickname)
                .orElse(UNKNOWN_NICKNAME);
    }
}
