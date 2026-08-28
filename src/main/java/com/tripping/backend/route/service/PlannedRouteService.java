package com.tripping.backend.route.service;

import com.tripping.backend.entity.PlannedRoute;
import com.tripping.backend.entity.PlannedRouteSpot;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.route.dto.*;
import com.tripping.backend.route.repository.PlannedRouteRepository;
import com.tripping.backend.route.repository.PlannedRouteSpotRepository;
import com.tripping.backend.place.repository.PlaceTouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlannedRouteService {

    private final PlannedRouteRepository plannedRouteRepository;
    private final PlannedRouteSpotRepository plannedRouteSpotRepository;
    private final PlaceTouristSpotRepository touristSpotRepository;

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

        List<RoutePlaceResponse> places = spots.stream()
                .map(spot -> new RoutePlaceResponse(spot, getSpotName(spot.getSpotId())))
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
        return new RoutePlaceResponse(saved, getSpotName(saved.getSpotId()));
    }

    // 5. 장소 순서, 내용 수정
    public RoutePlaceResponse updatePlace(Long userId, Long routeId, Long routePlaceId, RoutePlaceUpdateRequest request) {
        PlannedRoute route = getRouteOrThrow(routeId);
        validateOwner(route, userId);

        PlannedRouteSpot spot = plannedRouteSpotRepository.findById(routePlaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. id=" + routePlaceId));

        spot.setVisitOrder(request.getVisitOrder());
        PlannedRouteSpot updated = plannedRouteSpotRepository.save(spot);

        return new RoutePlaceResponse(updated, getSpotName(updated.getSpotId()));
    }

    // 6. 장소 삭제
    public void deletePlace(Long userId, Long routeId, Long routePlaceId) {
        PlannedRoute route = getRouteOrThrow(routeId);
        validateOwner(route, userId);

        plannedRouteSpotRepository.deleteById(routePlaceId);
    }

    // 루트 이름으로 검색
    public List<RouteSearchResponse> searchRoutesByTitle(String keyword) {
        return plannedRouteRepository.findByTitleContaining(keyword).stream()
                .map(RouteSearchResponse::new)
                .toList();
    }

    // 지도 불러오기 (저장된 장소 포함)
    public RouteMapResponse getRouteMap(Long routeId) {
        PlannedRoute route = getRouteOrThrow(routeId);
        List<PlannedRouteSpot> spots = plannedRouteSpotRepository.findByPlannedRouteId(routeId);

        List<RouteMapPlaceResponse> mapPlaces = spots.stream()
                .map(spot -> {
                    TouristSpot touristSpot = touristSpotRepository.findById(spot.getSpotId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. id=" + spot.getSpotId()));
                    return new RouteMapPlaceResponse(spot, touristSpot);
                })
                .toList();

        return new RouteMapResponse(route, mapPlaces);
    }

    // 공통: 루트 조회 (없으면 예외)
    private PlannedRoute getRouteOrThrow(Long routeId) {
        return plannedRouteRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 루트입니다. id=" + routeId));
    }

    // 공통: 본인 소유 루트인지 확인
    private void validateOwner(PlannedRoute route, Long userId) {
        if (!route.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 루트만 수정할 수 있습니다.");
        }
    }

    // 공통: 장소 이름 조회
    private String getSpotName(Long spotId) {
        return touristSpotRepository.findById(spotId)
                .map(TouristSpot::getName)
                .orElse("알 수 없는 장소");
    }
}