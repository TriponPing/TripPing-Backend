package com.tripping.backend.ping.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.WidgetPing;
import com.tripping.backend.ping.dto.OngoingTripResponse;
import com.tripping.backend.ping.dto.PingRegisterRequest;
import com.tripping.backend.ping.dto.PingResponse;
import com.tripping.backend.ping.repository.PingActualRouteRepository;
import com.tripping.backend.ping.repository.WidgetPingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.tripping.backend.ping.dto.SpotPingStatsResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PingService {

    private final PingActualRouteRepository actualRouteRepository;
    private final WidgetPingRepository widgetPingRepository;

    // 방문 장소 Ping 등록 - POST /routes/{routeId}/pings
    // 👈 원래는 "진행 중(IN_PROGRESS)인 여행에만" 등록 가능하도록 막혀 있었는데,
    // "다녀온(완료된) 여행"에 나중에 빠뜨린 장소를 추가하는 기능도 이 API를 쓰게 되면서
    // 그 제약이 걸림돌이 되어 제거함. 본인 소유 여행인지(findOwnedRoute)만 검증하면 충분함.
    @Transactional
    public PingResponse registerPing(Long userId, Long routeId, PingRegisterRequest request) {
        ActualRoute route = findOwnedRoute(userId, routeId);

        WidgetPing ping = WidgetPing.builder()
                .actualRouteId(routeId)
                .spotId(request.spotId())
                .placeName(request.placeName())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        return PingResponse.from(widgetPingRepository.save(ping));
    }

    // 진행 중 여행 조회 - GET /routes/{routeId}/pings (여행 정보 + 지금까지 찍은 핑 목록)
    public OngoingTripResponse getOngoingTrip(Long userId, Long routeId) {
        ActualRoute route = findOwnedRoute(userId, routeId);
        List<PingResponse> pings = widgetPingRepository
                .findByActualRouteIdAndIsDeletedFalseOrderByPingTimeAsc(routeId).stream()
                .map(PingResponse::from)
                .toList();

        return new OngoingTripResponse(
                route.getActualRouteId(),
                route.getStatus() != null ? route.getStatus().name() : null,
                route.getTravelDate(),
                pings
        );
    }

    // 여행 Ping 기록 조회 - GET /trips/{routeId}/pings (진행중/완료 상관없이 핑 목록만)
    public List<PingResponse> getTripPings(Long userId, Long routeId) {
        findOwnedRoute(userId, routeId); // 소유권 검증
        return widgetPingRepository
                .findByActualRouteIdAndIsDeletedFalseOrderByPingTimeAsc(routeId).stream()
                .map(PingResponse::from)
                .toList();
    }

    private ActualRoute findOwnedRoute(Long userId, Long routeId) {
        return actualRouteRepository.findByActualRouteIdAndUserIdAndIsDeletedFalse(routeId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행을 찾을 수 없습니다."));
    }

    // 특정 장소의 '인기 시간대'와 '총 핑 개수'를 계산해서 반환하는 메서드
    public SpotPingStatsResponse getSpotPingStats(Long spotId) {
        long totalCount = widgetPingRepository.countBySpotIdAndIsDeletedFalse(spotId);

        if (totalCount == 0) {
            return new SpotPingStatsResponse("정보 없음", 0);
        }

        List<Object[]> slotCounts = widgetPingRepository.countPingsByTimeSlot(spotId);

        String topTimeSlot = "정보 없음";
        if (!slotCounts.isEmpty()) {
            topTimeSlot = (String) slotCounts.get(0)[0];
        }

        return new SpotPingStatsResponse(topTimeSlot, totalCount);
    }
}