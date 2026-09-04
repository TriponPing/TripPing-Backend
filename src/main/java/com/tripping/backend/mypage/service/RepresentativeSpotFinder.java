package com.tripping.backend.mypage.service;

import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.mypage.repository.MyPageActualRouteSpotRepository;
import com.tripping.backend.mypage.repository.MyPageTouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 여러 여행(ActualRoute)에 대해 "대표 스팟"(방문 순서 1번째)의 이름/이미지/좌표를 한 번에 뽑아주는 헬퍼.
 * ActualRouteSpot과 TouristSpot 사이에 연관관계가 없어서(FK만 존재) 여기서 2단계로 조회 후 직접 매칭합니다.
 */
@Component
@RequiredArgsConstructor
public class RepresentativeSpotFinder {

    private final MyPageActualRouteSpotRepository actualRouteSpotRepository;
    private final MyPageTouristSpotRepository touristSpotRepository;

    public record RepresentativeSpot(
            Long spotId,
            String spotName,
            String imageUrl,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
    }

    public Map<Long, RepresentativeSpot> find(List<Long> actualRouteIds) {
        if (actualRouteIds.isEmpty()) {
            return Map.of();
        }

        List<ActualRouteSpot> spots = actualRouteSpotRepository
                .findByActualRouteIdInOrderByActualRouteIdAscVisitOrderAsc(actualRouteIds);

        // 여행별로 방문순서(visit_order)가 가장 빠른 첫 스팟만 남김
        Map<Long, ActualRouteSpot> firstSpotByRoute = new LinkedHashMap<>();
        for (ActualRouteSpot spot : spots) {
            firstSpotByRoute.putIfAbsent(spot.getActualRouteId(), spot);
        }

        List<Long> spotIds = firstSpotByRoute.values().stream()
                .map(ActualRouteSpot::getSpotId)
                .distinct()
                .toList();
        Map<Long, TouristSpot> touristSpotById = spotIds.isEmpty()
                ? Map.of()
                : touristSpotRepository.findAllById(spotIds).stream()
                        .collect(Collectors.toMap(TouristSpot::getSpotId, ts -> ts));

        Map<Long, RepresentativeSpot> result = new LinkedHashMap<>();
        firstSpotByRoute.forEach((routeId, spot) -> {
            TouristSpot ts = touristSpotById.get(spot.getSpotId());
            // 좌표는 spot(ActualRouteSpot)이 아니라 ts(TouristSpot)에서 가져와야 함.
            // ActualRouteSpot.latitude/longitude는 "핑 등록 시점 실제 GPS"용이라 핑을 안 찍으면 항상 null임.
            result.put(routeId, new RepresentativeSpot(
                    spot.getSpotId(),
                    ts != null ? ts.getName() : null,
                    ts != null ? ts.getImageUrl() : null,
                    ts != null ? ts.getLatitude() : null,
                    ts != null ? ts.getLongitude() : null
            ));
        });
        return result;
    }
}
