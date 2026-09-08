package com.tripping.backend.route.service;

import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.place.repository.PlaceTouristSpotRepository;
import com.tripping.backend.route.dto.RouteCandidateResponse;
import com.tripping.backend.route.dto.RouteCandidateSpotResponse;
import com.tripping.backend.route.dto.RouteRecommendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteRecommendService {

    private final PlaceTouristSpotRepository touristSpotRepository;

    private static final int CANDIDATE_COUNT = 4;
    private static final double WALK_SPEED_KM_PER_HOUR = 4.0;

    public List<RouteCandidateResponse> recommend(RouteRecommendRequest request) {
        // 1. 지역 기준으로 후보 장소 조회
        List<TouristSpot> allSpots = touristSpotRepository.findByRegionId(request.getRegionId());

        // 2. 제외하고 싶은 장소 걸러내기
        List<Long> excludeIds = request.getExcludeSpotIds() != null ? request.getExcludeSpotIds() : List.of();
        List<TouristSpot> candidates = allSpots.stream()
                .filter(spot -> !excludeIds.contains(spot.getSpotId()))
                .toList();

        if (candidates.size() < 2) {
            throw new IllegalArgumentException("추천 가능한 장소가 부족합니다. 지역 또는 제외 조건을 확인해주세요.");
        }

        // 3. 꼭 가고 싶은 장소들 (후보군 안에서)
        List<Long> mustVisitIds = request.getMustVisitSpotIds() != null ? request.getMustVisitSpotIds() : List.of();
        Map<Long, TouristSpot> candidateMap = new HashMap<>();
        for (TouristSpot spot : candidates) {
            candidateMap.put(spot.getSpotId(), spot);
        }

        List<TouristSpot> mustVisitSpots = new ArrayList<>();
        for (Long id : mustVisitIds) {
            TouristSpot spot = candidateMap.get(id);
            if (spot != null) {
                mustVisitSpots.add(spot);
            }
        }

        // 4. 서로 다른 시작점 4개 선정 (섞은 뒤 앞에서부터 선택)
        List<TouristSpot> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled);
        int startCount = Math.min(CANDIDATE_COUNT, shuffled.size());

        List<RouteCandidateResponse> results = new ArrayList<>();

        for (int i = 0; i < startCount; i++) {
            TouristSpot start = shuffled.get(i);
            results.add(buildRoute(i + 1, start, candidates, mustVisitSpots, request));
        }

        return results;
    }

    // 하나의 시작점에서 그리디 알고리즘으로 루트 하나 생성
    // 꼭 가야 할 장소가 남아있으면 그 안에서, 없으면 전체 후보 중에서 "가장 가까운 곳"을 선택
    private RouteCandidateResponse buildRoute(int order, TouristSpot start, List<TouristSpot> candidates,
                                              List<TouristSpot> mustVisitSpots, RouteRecommendRequest request) {
        int maxSpotCount = request.getMaxSpotCount() != null ? request.getMaxSpotCount() : 5;
        int walkTimeLimit = request.getWalkTimeLimit() != null ? request.getWalkTimeLimit() : Integer.MAX_VALUE;

        List<TouristSpot> route = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Set<Long> mustVisitIds = new HashSet<>();
        for (TouristSpot spot : mustVisitSpots) {
            mustVisitIds.add(spot.getSpotId());
        }

        route.add(start);
        visited.add(start.getSpotId());
        TouristSpot current = start;
        double totalDistance = 0.0;
        int totalTime = 0;

        while (route.size() < maxSpotCount) {
            // 아직 안 간 "꼭 가야 할 곳"이 있는지 확인
            List<TouristSpot> remainingMustVisit = mustVisitSpots.stream()
                    .filter(spot -> !visited.contains(spot.getSpotId()))
                    .toList();

            // 우선순위: 꼭 가야 할 곳이 남아있으면 그중에서, 없으면 전체 후보 중에서 선택
            List<TouristSpot> searchPool = !remainingMustVisit.isEmpty() ? remainingMustVisit : candidates;

            TouristSpot nearest = null;
            double nearestDistance = Double.MAX_VALUE;

            for (TouristSpot candidate : searchPool) {
                if (visited.contains(candidate.getSpotId())) continue;

                double distance = calculateDistance(current, candidate);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = candidate;
                }
            }

            if (nearest == null) break; // 더 이상 후보가 없으면 종료

            int additionalTime = (int) Math.round(nearestDistance / WALK_SPEED_KM_PER_HOUR * 60);

            // 꼭 가야 할 곳이면 시간 제한을 넘어도 강제로 포함 (필수 조건이 우선)
            boolean isMustVisit = mustVisitIds.contains(nearest.getSpotId());
            if (!isMustVisit && totalTime + additionalTime > walkTimeLimit) break;

            route.add(nearest);
            visited.add(nearest.getSpotId());
            totalDistance += nearestDistance;
            totalTime += additionalTime;
            current = nearest;
        }

        // 응답 DTO로 변환
        List<RouteCandidateSpotResponse> spotResponses = new ArrayList<>();
        for (int i = 0; i < route.size(); i++) {
            spotResponses.add(new RouteCandidateSpotResponse(route.get(i), i + 1));
        }

        String theme = determineTheme(route);

        return new RouteCandidateResponse(order, theme, round(totalDistance), totalTime, spotResponses);
    }

    // 루트 안 장소들의 category 비율로 테마 결정
    private String determineTheme(List<TouristSpot> route) {
        Map<String, Long> categoryCount = new HashMap<>();
        for (TouristSpot spot : route) {
            categoryCount.merge(spot.getCategory(), 1L, Long::sum);
        }

        String topCategory = categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        long topCount = categoryCount.getOrDefault(topCategory, 0L);
        boolean isEvenlyMixed = categoryCount.size() > 1 && topCount <= route.size() / 2.0;

        if (isEvenlyMixed) {
            return "종합 나들이 루트";
        }

        return switch (topCategory) {
            case "attraction" -> "역사탐방 루트";
            case "restaurant" -> "맛집투어 루트";
            case "cafe" -> "카페투어 루트";
            default -> "종합 나들이 루트";
        };
    }

    // Haversine 공식으로 두 장소 사이 거리(km) 계산
    private double calculateDistance(TouristSpot a, TouristSpot b) {
        double lat1 = a.getLatitude().doubleValue();
        double lon1 = a.getLongitude().doubleValue();
        double lat2 = b.getLatitude().doubleValue();
        double lon2 = b.getLongitude().doubleValue();

        double r = 6371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double x = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double y = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));

        return r * y;
    }

    private double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}