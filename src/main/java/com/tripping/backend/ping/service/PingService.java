package com.tripping.backend.ping.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.RouteStatus;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.entity.WidgetPing;
import com.tripping.backend.ping.dto.AddTripSpotRequest;
import com.tripping.backend.ping.dto.AddTripSpotResponse;
import com.tripping.backend.ping.dto.OngoingTripResponse;
import com.tripping.backend.ping.dto.PingRegisterRequest;
import com.tripping.backend.ping.dto.PingResponse;
import com.tripping.backend.ping.dto.SpotPingStatsResponse;
import com.tripping.backend.ping.repository.PingActualRouteRepository;
import com.tripping.backend.ping.repository.PingActualRouteSpotRepository;
import com.tripping.backend.ping.repository.PingTouristSpotRepository;
import com.tripping.backend.ping.repository.WidgetPingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PingService {

    private final PingActualRouteRepository actualRouteRepository;
    private final PingActualRouteSpotRepository actualRouteSpotRepository;
    private final PingTouristSpotRepository touristSpotRepository;
    private final WidgetPingRepository widgetPingRepository;

    // 방문 장소 Ping 등록 - POST /routes/{routeId}/pings
    // 👈 진행 중(IN_PROGRESS)인 여행 전용 - 완료된 여행에 놓친 장소를 추가하려면
    // POST /trips/{routeId}/spots (addSpotToTrip)를 대신 쓸 것
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

    // 👈 새로 추가: 완료된 여행에 놓친 방문 스팟을 나중에 추가 - POST /trips/{routeId}/spots
    // WIDGET_PING이 아니라 ACTUAL_ROUTE_SPOT에 직접 저장함 (이미 끝난 여행이라 "확정된 방문 기록"이므로).
    // ⚠️ ActualRouteSpot.builder() 구성은 다른 엔티티 빌더 패턴을 보고 추측했습니다.
    // 실제 엔티티 파일과 다르면 알려주세요.
    @Transactional
    public AddTripSpotResponse addSpotToTrip(Long userId, Long routeId, AddTripSpotRequest request) {
        findOwnedRoute(userId, routeId); // 소유권 검증

        List<ActualRouteSpot> existingSpots = actualRouteSpotRepository
                .findByActualRouteIdOrderByVisitOrderAsc(routeId);
        int nextVisitOrder = existingSpots.stream()
                .mapToInt(ActualRouteSpot::getVisitOrder)
                .max()
                .orElse(0) + 1;

        ActualRouteSpot spot = ActualRouteSpot.builder()
                .actualRouteId(routeId)
                .spotId(request.spotId())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .visitOrder(nextVisitOrder)
                .visitTime(LocalDateTime.now())
                .build();

        ActualRouteSpot saved = actualRouteSpotRepository.save(spot);
        TouristSpot ts = touristSpotRepository.findById(request.spotId()).orElse(null);

        return new AddTripSpotResponse(
                saved.getVisitOrder(),
                saved.getSpotId(),
                ts != null ? ts.getName() : null,
                ts != null ? ts.getCategory() : null,
                ts != null ? ts.getAddress() : null,
                ts != null ? ts.getLatitude() : null,
                ts != null ? ts.getLongitude() : null,
                saved.getVisitTime()
        );
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