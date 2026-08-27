package com.tripping.backend.route.service;

import com.tripping.backend.entity.PlannedRoute;
import com.tripping.backend.entity.PlannedRouteSpot;
import com.tripping.backend.route.dto.*;
import com.tripping.backend.route.repository.PlannedRouteRepository;
import com.tripping.backend.route.repository.PlannedRouteSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlannedRouteService {

    private final PlannedRouteRepository plannedRouteRepository;
    private final PlannedRouteSpotRepository plannedRouteSpotRepository;

    // 1. 루트 생성
    public RouteResponse createRoute(Long userId, RouteCreateRequest request) {
        PlannedRoute route = PlannedRoute.builder()
                .userId(userId)
                .candidateId(request.getCandidateId())
                .title(request.getTitle())
                .build();

        PlannedRoute saved = plannedRouteRepository.save(route);
        return new RouteResponse(saved, List.of());
    }

    // 2. 루트 상세 조회
    public RouteResponse getRouteDetail(Long routeId) {
        PlannedRoute route = getRouteOrThrow(routeId);
        List<PlannedRouteSpot> spots = plannedRouteSpotRepository.findByPlannedRouteId(routeId);

        // TODO: feat/search merge 후 실제 장소 이름(TouristSpotRepository) 연동 예정
        List<RoutePlaceResponse> places = spots.stream()
                .map(spot -> new RoutePlaceResponse(spot, "장소명 연동 예정"))
                .toList();

        return new RouteResponse(route, places);
    }

    // 4. 장소 추가
    public RoutePlaceResponse addPlace(Long userId, Long routeId, RoutePlaceRequest request) {
        PlannedRoute route = getRouteOrThrow(routeId);
        validateOwner(route, userId);

        PlannedRouteSpot spot = PlannedRouteSpot.builder()
                .plannedRouteId(routeId)
                .spotId(request.getSpotId())
                .visitOrder(request.getVisitOrder())
                .build();

        PlannedRouteSpot saved = plannedRouteSpotRepository.save(spot);
        return new RoutePlaceResponse(saved, "장소명 연동 예정");
    }

    // 5. 장소 순서, 내용 수정
    public RoutePlaceResponse updatePlace(Long userId, Long routeId, Long routePlaceId, RoutePlaceUpdateRequest request) {
        PlannedRoute route = getRouteOrThrow(routeId);
        validateOwner(route, userId);

        PlannedRouteSpot spot = plannedRouteSpotRepository.findById(routePlaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. id=" + routePlaceId));

        spot.setVisitOrder(request.getVisitOrder());
        PlannedRouteSpot updated = plannedRouteSpotRepository.save(spot);

        return new RoutePlaceResponse(updated, "장소명 연동 예정");
    }

    // 6. 장소 삭제
    public void deletePlace(Long userId, Long routeId, Long routePlaceId) {
        PlannedRoute route = getRouteOrThrow(routeId);
        validateOwner(route, userId);

        plannedRouteSpotRepository.deleteById(routePlaceId);
    }

    private PlannedRoute getRouteOrThrow(Long routeId) {
        return plannedRouteRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 루트입니다. id=" + routeId));
    }

    private void validateOwner(PlannedRoute route, Long userId) {
        if (!route.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 루트만 수정할 수 있습니다.");
        }
    }
}