package com.tripping.backend.trip.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.PlannedRoute;
import com.tripping.backend.entity.PlannedRouteSpot;
import com.tripping.backend.route.repository.PlannedRouteRepository;
import com.tripping.backend.route.repository.PlannedRouteSpotRepository;
import com.tripping.backend.trip.dto.TripCreateRequest;
import com.tripping.backend.trip.dto.TripResponse;
import com.tripping.backend.trip.repository.TripActualRouteRepository;
import com.tripping.backend.trip.repository.TripActualRouteSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final PlannedRouteRepository plannedRouteRepository;
    private final PlannedRouteSpotRepository plannedRouteSpotRepository;
    private final TripActualRouteRepository actualRouteRepository;
    private final TripActualRouteSpotRepository actualRouteSpotRepository;

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
}