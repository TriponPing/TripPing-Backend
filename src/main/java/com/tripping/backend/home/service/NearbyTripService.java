package com.tripping.backend.home.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.home.dto.response.PopularTripResponse;
import com.tripping.backend.home.repository.HomeActualRouteRepository;
import com.tripping.backend.home.repository.HomeActualRouteSpotRepository;
import com.tripping.backend.home.repository.NearbyRouteProjection;
import com.tripping.backend.mypage.dto.PageResponse;
import java.time.LocalDateTime;
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

    /** 기본 반경(km). 프론트에서 radiusKm 을 안 보내면 이 값을 씁니다. */
    public static final double DEFAULT_RADIUS_KM = 10.0;

    private final HomeActualRouteSpotRepository actualRouteSpotRepository;
    private final HomeActualRouteRepository actualRouteRepository;
    // 👈 수정: 카드 하나를 채우는 로직(작성자/스탑목록/사진/저장수/테마명)이 "이번 주 인기 루트"와
    // 완전히 같아서(거리만 추가) NearbyTripResponse를 따로 안 쓰고 PopularTripService.buildTripResponse()를
    // 재사용함 - PopularTripResponse에 distanceKm을 nullable로 추가해서 같은 DTO를 공유함.
    private final PopularTripService popularTripService;

    public PageResponse<PopularTripResponse> getNearbyTrips(
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

        // "저장수" 표시는 전체 기간 기준 (내 주변 코스는 "이번주"처럼 기간 한정 리스트가 아니라서
        // getTripDetail()과 동일하게 넓은 기간을 씀 - 진짜 전체 카운트 메서드 생기면 교체)
        LocalDateTime savedCountSince = LocalDateTime.now().minusYears(100);

        List<PopularTripResponse> content = nearbyPage.getContent().stream()
                .map(projection -> {
                    ActualRoute route = routeById.get(projection.getActualRouteId());
                    if (route == null) {
                        return null;
                    }
                    return popularTripService.buildTripResponse(route, savedCountSince).toBuilder()
                            .distanceKm(projection.getDistanceKm())
                            .build();
                })
                .filter(response -> response != null)
                .toList();

        return PageResponse.of(content, nearbyPage);
    }
}
