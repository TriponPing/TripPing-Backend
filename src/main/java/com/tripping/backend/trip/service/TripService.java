package com.tripping.backend.trip.service;

import com.tripping.backend.trip.dto.CurrentTripResponse;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.PlannedRoute;
import com.tripping.backend.entity.PlannedRouteSpot;
import com.tripping.backend.route.repository.PlannedRouteRepository;
import com.tripping.backend.route.repository.PlannedRouteSpotRepository;
import com.tripping.backend.trip.dto.TripCreateRequest;
import com.tripping.backend.trip.dto.TripResponse;
import com.tripping.backend.trip.dto.TripRouteMapPlaceResponse;
import com.tripping.backend.trip.dto.TripRouteMapSearchResponse;
import com.tripping.backend.trip.repository.TripActualRouteRepository;
import com.tripping.backend.trip.repository.TripActualRouteSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final PlannedRouteRepository plannedRouteRepository;
    private final PlannedRouteSpotRepository plannedRouteSpotRepository;
    private final TripActualRouteRepository actualRouteRepository;
    private final TripActualRouteSpotRepository actualRouteSpotRepository;
    private final com.tripping.backend.home.repository.HomeSavedRouteRepository savedRouteRepository;

    // ⭐️ [수정] 유저에게 진행중(IN_PROGRESS)인 여행이 실수로 2개 이상 남아있어도
    // 에러 안 나게 Pageable로 딱 1개만(가장 최근 것) 가져오도록 변경
    public CurrentTripResponse getCurrentInProgressRoute(Long userId) {
        List<ActualRoute> routes = actualRouteRepository
                .findInProgressRoutes(userId, PageRequest.of(0, 1));

        return routes.isEmpty() ? null : new CurrentTripResponse(routes.get(0));
    }

    // 실제 여행으로 저장 (PlannedRoute -> ActualRoute 복사)
    public TripResponse createTrip(Long userId, Long routeId, TripCreateRequest request) {
        // 1. 원본 계획 루트 조회 (본인 것인지 확인)
        PlannedRoute plannedRoute = plannedRouteRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 루트입니다. id=" + routeId));

        if (!plannedRoute.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 루트만 여행으로 전환할 수 있습니다.");
        }

        // 2. ActualRoute 새로 생성 (복사)
        ActualRoute actualRoute = ActualRoute.builder()
                .userId(userId)
                .travelDate(request.getTravelDate())
                .companionType(request.getCompanionType())
                .transport(request.getTransport())
                .memberCount(request.getMemberCount())
                .build();

        ActualRoute savedActualRoute = actualRouteRepository.save(actualRoute);

        // 2-1. 계획을 "시작함"으로 표시 - 나의 여행 지도 > 내 계획 탭에서 더 이상 안 보이게
        plannedRoute.setIsStarted(true);
        plannedRouteRepository.save(plannedRoute);

        // 3. PlannedRouteSpot들을 ActualRouteSpot으로 복사
        List<PlannedRouteSpot> plannedSpots = plannedRouteSpotRepository.findByPlannedRouteId(routeId);

        for (PlannedRouteSpot plannedSpot : plannedSpots) {
            ActualRouteSpot actualSpot = ActualRouteSpot.builder()
                    .actualRouteId(savedActualRoute.getActualRouteId())
                    .spotId(plannedSpot.getSpotId())
                    .visitOrder(plannedSpot.getVisitOrder())
                    .build();
            actualRouteSpotRepository.save(actualSpot);
        }

        return new TripResponse(savedActualRoute);
    }

    // 지도용 루트 검색 (지역/카테고리 필터)
    public java.util.List<TripRouteMapSearchResponse> searchRoutesForMap(String regionId, String category) {
        java.util.List<Long> routeIds = actualRouteRepository.findMatchingRouteIds(regionId, category);

        return routeIds.stream()
                .map(routeId -> {
                    java.util.List<TripRouteMapPlaceResponse> places = actualRouteSpotRepository
                            .findMapSpotsByActualRouteId(routeId)
                            .stream()
                            .map(TripRouteMapPlaceResponse::new)
                            .toList();
                    return new TripRouteMapSearchResponse(routeId, places);
                })
                .toList();
    }

    public List<TripRouteMapSearchResponse> getSavedRoutesForMap(Long userId) {
        List<Long> routeIds = savedRouteRepository.findActualRouteIdsByUserId(userId);

        return routeIds.stream()
                .map(routeId -> {
                    List<TripRouteMapPlaceResponse> places = actualRouteSpotRepository
                            .findMapSpotsByActualRouteId(routeId)
                            .stream()
                            .map(TripRouteMapPlaceResponse::new)
                            .toList();
                    return new TripRouteMapSearchResponse(routeId, places);
                })
                .toList();
    }
}