package com.tripping.backend.mypage.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.PlannedRoute;
import com.tripping.backend.entity.PlannedRouteSpot;
import com.tripping.backend.entity.SavedRoute;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.mypage.dto.MapDetailResponse;
import com.tripping.backend.mypage.dto.MapPinResponse;
import com.tripping.backend.mypage.dto.MapSearchResponse;
import com.tripping.backend.mypage.dto.MyMapResponse;
import com.tripping.backend.mypage.repository.MyPageActualRouteRepository;
import com.tripping.backend.mypage.repository.MyPageActualRouteSpotRepository;
import com.tripping.backend.mypage.repository.MyPageSavedRouteRepository;
import com.tripping.backend.mypage.repository.MyPageTouristSpotRepository;
import com.tripping.backend.route.repository.PlannedRouteRepository;
import com.tripping.backend.route.repository.PlannedRouteSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 나의 여행 지도 (프론트 지도 SDK에 뿌려줄 좌표 데이터를 만드는 역할만 함 - 지도를 직접 그리지 않음)
 *
 * drawn = 내가 다녀온 여행(ACTUAL_ROUTE, 소유자 본인)
 * saved = 내가 저장한 루트(SAVED_ROUTE가 참조하는 ACTUAL_ROUTE)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageMapService {

    private static final String TYPE_DRAWN = "DRAWN";
    private static final String TYPE_SAVED = "SAVED";
    private static final String TYPE_PLANNED = "PLANNED";

    private final MyPageActualRouteRepository actualRouteRepository;
    private final MyPageActualRouteSpotRepository actualRouteSpotRepository;
    private final MyPageSavedRouteRepository savedRouteRepository;
    private final MyPageTouristSpotRepository touristSpotRepository;
    private final PlannedRouteRepository plannedRouteRepository;
    private final PlannedRouteSpotRepository plannedRouteSpotRepository;
    private final RepresentativeSpotFinder representativeSpotFinder;

    // 나의 여행 지도 조회 - GET /users/me/map
    public MyMapResponse getMyMap(Long userId) {
        List<ActualRoute> drawnRoutes = actualRouteRepository.findByUserIdAndIsDeletedFalse(userId);
        List<SavedRoute> savedRoutes = savedRouteRepository.findByUserId(userId);

        List<Long> drawnIds = drawnRoutes.stream().map(ActualRoute::getActualRouteId).toList();
        List<Long> savedIds = savedRoutes.stream().map(SavedRoute::getActualRouteId).toList();
        List<Long> allIds = Stream.concat(drawnIds.stream(), savedIds.stream()).distinct().toList();

        Map<Long, ActualRoute> routeById = actualRouteRepository.findAllById(allIds).stream()
                .collect(Collectors.toMap(ActualRoute::getActualRouteId, r -> r));
        Map<Long, RepresentativeSpotFinder.RepresentativeSpot> repByRoute = representativeSpotFinder.find(allIds);

        List<MapPinResponse> pins = new ArrayList<>();
        for (Long routeId : drawnIds) {
            pins.add(toPin(routeById.get(routeId), repByRoute.get(routeId), TYPE_DRAWN));
        }
        for (Long routeId : savedIds) {
            pins.add(toPin(routeById.get(routeId), repByRoute.get(routeId), TYPE_SAVED));
        }

        // "다녀온 장소" 개수 - 저장한 루트는 빼고, 내가 실제로 다녀온 여행에 포함된 장소만
        // 중복(같은 곳 여러 번 방문) 제거해서 셈
        int visitedPlaceCount = drawnIds.isEmpty() ? 0 : (int) actualRouteSpotRepository
                .findByActualRouteIdInOrderByActualRouteIdAscVisitOrderAsc(drawnIds).stream()
                .map(ActualRouteSpot::getSpotId)
                .distinct()
                .count();

        return new MyMapResponse(pins, visitedPlaceCount);
    }

    // 나의 여행 지도 상세 조회 - GET /users/me/map/detail?type=drawn|saved|planned
    public List<MapDetailResponse> getMyMapDetail(Long userId, String type) {
        if ("planned".equalsIgnoreCase(type)) {
            return getPlannedMapDetail(userId);
        }

        boolean isSaved = "saved".equalsIgnoreCase(type);

        List<Long> routeIds = isSaved
                ? savedRouteRepository.findByUserId(userId).stream().map(SavedRoute::getActualRouteId).toList()
                : actualRouteRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                        .map(ActualRoute::getActualRouteId).toList();

        if (routeIds.isEmpty()) {
            return List.of();
        }

        Map<Long, ActualRoute> routeById = actualRouteRepository.findAllById(routeIds).stream()
                .collect(Collectors.toMap(ActualRoute::getActualRouteId, r -> r));

        List<ActualRouteSpot> spots = actualRouteSpotRepository
                .findByActualRouteIdInOrderByActualRouteIdAscVisitOrderAsc(routeIds);

        List<Long> touristSpotIds = spots.stream().map(ActualRouteSpot::getSpotId).distinct().toList();
        Map<Long, TouristSpot> touristSpotById = touristSpotIds.isEmpty()
                ? Map.of()
                : touristSpotRepository.findAllById(touristSpotIds).stream()
                        .collect(Collectors.toMap(TouristSpot::getSpotId, ts -> ts));

        Map<Long, List<ActualRouteSpot>> spotsByRoute = spots.stream()
                .collect(Collectors.groupingBy(ActualRouteSpot::getActualRouteId, LinkedHashMap::new, Collectors.toList()));

        String typeLabel = isSaved ? TYPE_SAVED : TYPE_DRAWN;

        return routeIds.stream()
                .map(routeId -> {
                    ActualRoute route = routeById.get(routeId);
                    List<MapDetailResponse.SpotPoint> spotPoints = spotsByRoute.getOrDefault(routeId, List.of()).stream()
                            .map(spot -> {
                                TouristSpot ts = touristSpotById.get(spot.getSpotId());
                                // 좌표는 ts(TouristSpot)에서 - ActualRouteSpot.latitude/longitude는
                                // "핑 등록 시점 실제 GPS"용이라 핑을 안 찍으면 항상 null임.
                                return new MapDetailResponse.SpotPoint(
                                        spot.getVisitOrder(),
                                        spot.getSpotId(),
                                        ts != null ? ts.getName() : null,
                                        ts != null ? ts.getLatitude() : null,
                                        ts != null ? ts.getLongitude() : null,
                                        spot.getVisitTime()
                                );
                            })
                            .toList();
                    return new MapDetailResponse(
                            routeId,
                            route != null ? route.getTravelDate() : null,
                            typeLabel,
                            spotPoints
                    );
                })
                .toList();
    }

    // 나의 여행 지도 > 내 계획 - 아직 실제 여행으로 시작 안 한 계획들의 전체 경로
    private List<MapDetailResponse> getPlannedMapDetail(Long userId) {
        List<PlannedRoute> plans = plannedRouteRepository.findByUserIdAndIsDeletedFalseAndIsStartedFalse(userId);
        if (plans.isEmpty()) {
            return List.of();
        }

        List<Long> planIds = plans.stream().map(PlannedRoute::getPlannedRouteId).toList();
        List<PlannedRouteSpot> spots = plannedRouteSpotRepository
                .findByPlannedRouteIdInOrderByPlannedRouteIdAscVisitOrderAsc(planIds);

        List<Long> touristSpotIds = spots.stream().map(PlannedRouteSpot::getSpotId).distinct().toList();
        Map<Long, TouristSpot> touristSpotById = touristSpotIds.isEmpty()
                ? Map.of()
                : touristSpotRepository.findAllById(touristSpotIds).stream()
                        .collect(Collectors.toMap(TouristSpot::getSpotId, ts -> ts));

        Map<Long, List<PlannedRouteSpot>> spotsByPlan = spots.stream()
                .collect(Collectors.groupingBy(PlannedRouteSpot::getPlannedRouteId, LinkedHashMap::new, Collectors.toList()));

        return plans.stream()
                .map(plan -> {
                    List<MapDetailResponse.SpotPoint> spotPoints = spotsByPlan.getOrDefault(plan.getPlannedRouteId(), List.of()).stream()
                            .map(spot -> {
                                TouristSpot ts = touristSpotById.get(spot.getSpotId());
                                return new MapDetailResponse.SpotPoint(
                                        spot.getVisitOrder(),
                                        spot.getSpotId(),
                                        ts != null ? ts.getName() : null,
                                        ts != null ? ts.getLatitude() : null,
                                        ts != null ? ts.getLongitude() : null,
                                        null // 계획 단계라 실제 방문 시각은 없음
                                );
                            })
                            .toList();
                    return new MapDetailResponse(
                            plan.getPlannedRouteId(),
                            plan.getCreatedAt() != null ? plan.getCreatedAt().toLocalDate() : null,
                            TYPE_PLANNED,
                            spotPoints
                    );
                })
                .toList();
    }

    // 지도 검색 - GET /users/me/map/search?name= (포함된 관광지 이름 기준)
    public List<MapSearchResponse> search(Long userId, String keyword) {
        List<Long> drawnRouteIds = actualRouteRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .map(ActualRoute::getActualRouteId).toList();
        List<Long> savedRouteIds = savedRouteRepository.findByUserId(userId).stream()
                .map(SavedRoute::getActualRouteId).toList();

        List<MapSearchResponse> result = new ArrayList<>();
        result.addAll(buildSearchResult(drawnRouteIds, keyword, TYPE_DRAWN));
        result.addAll(buildSearchResult(savedRouteIds, keyword, TYPE_SAVED));
        return result;
    }

    private List<MapSearchResponse> buildSearchResult(List<Long> candidateRouteIds, String keyword, String type) {
        if (candidateRouteIds.isEmpty()) {
            return List.of();
        }
        List<Long> matchedIds = actualRouteSpotRepository.findRouteIdsBySpotNameKeyword(candidateRouteIds, keyword);
        if (matchedIds.isEmpty()) {
            return List.of();
        }
        return actualRouteRepository.findAllById(matchedIds).stream()
                .map(route -> new MapSearchResponse(route.getActualRouteId(), type, route.getTravelDate()))
                .toList();
    }

    private MapPinResponse toPin(ActualRoute route, RepresentativeSpotFinder.RepresentativeSpot rep, String type) {
        return new MapPinResponse(
                route != null ? route.getActualRouteId() : null,
                type,
                route != null ? route.getTravelDate() : null,
                rep != null ? rep.latitude() : null,
                rep != null ? rep.longitude() : null,
                rep != null ? rep.spotName() : null
        );
    }
}
