package com.tripping.backend.community.service;

import com.tripping.backend.community.dto.response.RouteSummaryResponse;
import com.tripping.backend.community.repository.ActualRouteRepository;
import com.tripping.backend.community.repository.ActualRouteSpotRepository;
import com.tripping.backend.community.repository.AppUserRepository;
import com.tripping.backend.community.repository.RegionRepository;
import com.tripping.backend.community.repository.TouristSpotRepository;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.AppUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private static final String UNKNOWN_NICKNAME = "알 수 없음";

    private final ActualRouteRepository actualRouteRepository;
    private final ActualRouteSpotRepository actualRouteSpotRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final RegionRepository regionRepository;
    private final AppUserRepository appUserRepository;

    /**
     * ActualRoute 에는 region_id 컬럼이 없어서
     * TouristSpot(region_id) -> ActualRouteSpot(spot_id) -> ActualRoute(actual_route_id)
     * 순서로 3단계 조회를 거쳐 해당 지역을 지나간 루트만 찾습니다.
     * (엔티티 간 연관관계가 없어서 JOIN 대신 ID 목록으로 좁혀나가는 방식입니다.)
     */
    public Page<RouteSummaryResponse> getRoutesByRegion(String regionId, Pageable pageable) {
        if (!regionRepository.existsById(regionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 지역입니다. regionId=" + regionId);
        }

        List<Long> spotIds = touristSpotRepository.findSpotIdsByRegionId(regionId);
        if (spotIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> routeIds = actualRouteSpotRepository.findDistinctActualRouteIdsBySpotIdIn(spotIds);
        if (routeIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<ActualRoute> routes = actualRouteRepository
                .findByActualRouteIdInAndIsPublicTrueAndIsDeletedFalseOrderByCreatedAtDesc(routeIds, pageable);

        return routes.map(route -> RouteSummaryResponse.from(route, findNickname(route.getUserId())));
    }

    private String findNickname(Long userId) {
        return appUserRepository.findById(userId)
                .map(AppUser::getNickname)
                .orElse(UNKNOWN_NICKNAME);
    }
}
