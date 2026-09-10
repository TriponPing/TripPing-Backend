package com.tripping.backend.mypage.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.PingLog;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.mypage.dto.PageResponse;
import com.tripping.backend.mypage.dto.TripDetailResponse;
import com.tripping.backend.mypage.dto.TripSummaryResponse;
import com.tripping.backend.mypage.repository.MyPageActualRouteRepository;
import com.tripping.backend.mypage.repository.MyPageActualRouteSpotRepository;
import com.tripping.backend.mypage.repository.MyPagePingLogRepository;
import com.tripping.backend.mypage.repository.MyPageTouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageTripService {

    private final MyPageActualRouteRepository actualRouteRepository;
    private final MyPageActualRouteSpotRepository actualRouteSpotRepository;
    private final MyPagePingLogRepository pingLogRepository;
    private final MyPageTouristSpotRepository touristSpotRepository;
    private final RepresentativeSpotFinder representativeSpotFinder;

    // 다녀온 여행 목록 조회(요약) - GET /users/me/trips/recent
    public List<TripSummaryResponse> getRecentTrips(Long userId) {
        List<ActualRoute> routes = actualRouteRepository
                .findTop5ByUserIdAndIsDeletedFalseOrderByTravelDateDesc(userId);
        return toSummaries(routes);
    }

    // 다녀온 여행 전체 목록 조회 - GET /users/me/trips
    public PageResponse<TripSummaryResponse> getAllTrips(Long userId, Pageable pageable) {
        Page<ActualRoute> page = actualRouteRepository
                .findByUserIdAndIsDeletedFalseOrderByTravelDateDesc(userId, pageable);
        return PageResponse.of(toSummaries(page.getContent()), page);
    }

    private List<TripSummaryResponse> toSummaries(List<ActualRoute> routes) {
        if (routes.isEmpty()) {
            return List.of();
        }

        List<Long> routeIds = routes.stream().map(ActualRoute::getActualRouteId).toList();
        Map<Long, RepresentativeSpotFinder.RepresentativeSpot> repByRoute = representativeSpotFinder.find(routeIds);

        // 여행별 방문 장소 개수 ("핑 N개" 표시용) - 한 번에 조회해서 N+1 방지
        Map<Long, Long> placeCountByRoute = actualRouteSpotRepository
                .findByActualRouteIdInOrderByActualRouteIdAscVisitOrderAsc(routeIds).stream()
                .collect(Collectors.groupingBy(ActualRouteSpot::getActualRouteId, Collectors.counting()));

        return routes.stream()
                .map(route -> {
                    RepresentativeSpotFinder.RepresentativeSpot rep = repByRoute.get(route.getActualRouteId());
                    Long placeCount = placeCountByRoute.get(route.getActualRouteId());
                    return new TripSummaryResponse(
                            route.getActualRouteId(),
                            route.getTravelDate(),
                            route.getStatus() != null ? route.getStatus().name() : null,
                            route.getMemberCount(),
                            rep != null ? rep.spotName() : null,
                            rep != null ? rep.imageUrl() : null,
                            placeCount != null ? placeCount.intValue() : 0
                    );
                })
                .toList();
    }

    // 여행 기록 상세 조회 - GET /users/me/trips/{tripId}
    // 본인이 다녀온 여행뿐 아니라, 다른 사람이 공개해둔 루트를 저장(북마크)해서 보는 경우도 있어서
    // 소유자가 아니어도 공개(isPublic) 루트면 조회 가능하게 함. (저장한 여행 카드 -> 상세보기, 여행 바로 시작하기)
    public TripDetailResponse getTripDetail(Long userId, Long tripId) {
        ActualRoute route = actualRouteRepository
                .findByActualRouteIdAndIsDeletedFalse(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행 기록을 찾을 수 없습니다."));

        boolean isOwner = route.getUserId().equals(userId);
        boolean isPublic = Boolean.TRUE.equals(route.getIsPublic());
        if (!isOwner && !isPublic) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비공개 루트입니다.");
        }

        List<ActualRouteSpot> spots = actualRouteSpotRepository
                .findByActualRouteIdOrderByVisitOrderAsc(tripId);

        List<Long> spotIds = spots.stream().map(ActualRouteSpot::getActualRouteSpotId).toList();
        List<PingLog> logs = spotIds.isEmpty()
                ? List.of()
                : pingLogRepository.findByActualRouteSpotIdInAndIsDeletedFalse(spotIds);

        // 스팟 하나에 로그가 여러 개일 가능성을 배제할 수 없어 마지막에 매칭된 것으로 덮어씀(최신 우선 정렬 필요하면 정렬 추가)
        Map<Long, PingLog> logBySpot = new HashMap<>();
        for (PingLog log : logs) {
            logBySpot.put(log.getActualRouteSpotId(), log);
        }

        List<Long> touristSpotIds = spots.stream().map(ActualRouteSpot::getSpotId).distinct().toList();
        Map<Long, TouristSpot> touristSpotById = touristSpotIds.isEmpty()
                ? Map.of()
                : touristSpotRepository.findAllById(touristSpotIds).stream()
                        .collect(Collectors.toMap(TouristSpot::getSpotId, ts -> ts));

        List<TripDetailResponse.SpotDetail> spotDetails = spots.stream()
                .map(spot -> {
                    PingLog log = logBySpot.get(spot.getActualRouteSpotId());
                    TouristSpot ts = touristSpotById.get(spot.getSpotId());
                    // 좌표는 ts(TouristSpot)에서 - ActualRouteSpot.latitude/longitude는
                    // "핑 등록 시점 실제 GPS"용이라 핑을 안 찍으면 항상 null임.
                    return new TripDetailResponse.SpotDetail(
                            spot.getActualRouteSpotId(),
                            spot.getVisitOrder(),
                            spot.getSpotId(),
                            ts != null ? ts.getName() : null,
                            ts != null ? ts.getCategory() : null,
                            ts != null ? ts.getAddress() : null,
                            ts != null ? ts.getLatitude() : null,
                            ts != null ? ts.getLongitude() : null,
                            spot.getVisitTime(),
                            log != null ? log.getRating() : null,
                            log != null ? log.getPhotoUrl() : null,
                            log != null ? log.getReviewComment() : null
                    );
                })
                .toList();

        return new TripDetailResponse(
                route.getActualRouteId(),
                route.getTravelDate(),
                route.getCompanionType(),
                route.getTransport(),
                route.getMemberCount(),
                route.getStatus() != null ? route.getStatus().name() : null,
                route.getIsPublic(),
                spotDetails
        );
    }
}
