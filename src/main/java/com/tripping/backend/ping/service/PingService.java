package com.tripping.backend.ping.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.RouteStatus;
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
    @Transactional
    public PingResponse registerPing(Long userId, Long routeId, PingRegisterRequest request) {
        ActualRoute route = findOwnedRoute(userId, routeId);
        if (route.getStatus() != RouteStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "진행 중인 여행에만 핑을 등록할 수 있습니다.");
        }

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

    // 💡 특정 장소의 '인기 시간대'와 '총 핑 개수'를 계산해서 반환하는 메서드
    public SpotPingStatsResponse getSpotPingStats(Long spotId) {
        // 1. 총 핑 개수 조회
        long totalCount = widgetPingRepository.countBySpotIdAndIsDeletedFalse(spotId);

        if (totalCount == 0) {
            return new SpotPingStatsResponse("정보 없음", 0);
        }

        // 2. 시간대별 핑 개수 집계 결과 가져오기 (내림차순 정렬되어 있음)
        List<Object[]> slotCounts = widgetPingRepository.countPingsByTimeSlot(spotId);

        // 3. 가장 핑이 많이 찍힌 1위 시간대 추출 (첫 번째 결과의 0번째 인덱스가 timeSlot 문자열)
        String topTimeSlot = "정보 없음";
        if (!slotCounts.isEmpty()) {
            topTimeSlot = (String) slotCounts.get(0)[0];
        }

        return new SpotPingStatsResponse(topTimeSlot, totalCount);
    }


}
