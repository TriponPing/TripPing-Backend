package com.tripping.backend.home.service;

import com.tripping.backend.auth.repository.UserRepository;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.home.dto.response.CoordinateResponse;
import com.tripping.backend.home.dto.response.PopularTripResponse;
import com.tripping.backend.home.repository.HomeActualRouteRepository;
import com.tripping.backend.home.repository.HomeActualRouteSpotRepository;
import com.tripping.backend.home.repository.HomeSavedRouteRepository;
import com.tripping.backend.home.repository.HomeTouristSpotRepository;
import com.tripping.backend.home.dto.response.TripDetailResponse;
import com.tripping.backend.home.dto.response.StopSummaryResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularTripService {

    private static final String UNKNOWN_NICKNAME = "알 수 없음";

    /** "이번주" 판단 기준 일수. period 값이 늘어나면(예: month) resolveWindowDays()만 확장하면 됩니다. */
    private static final int WEEK_WINDOW_DAYS = 7;

    private final HomeSavedRouteRepository savedRouteRepository;
    private final HomeActualRouteRepository actualRouteRepository;
    private final HomeActualRouteSpotRepository actualRouteSpotRepository;
    private final HomeTouristSpotRepository touristSpotRepository;
    private final UserRepository userRepository; // auth 도메인의 AppUser Repository를 그대로 재사용합니다.

    public List<PopularTripResponse> getPopularTrips(String period, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(resolveWindowDays(period));
        Pageable topN = PageRequest.of(0, limit);

        List<Long> popularRouteIds = savedRouteRepository.findPopularRouteIds(since, topN);
        if (popularRouteIds.isEmpty()) {
            return List.of();
        }

        // findByActualRouteIdIn... 은 순서를 보장하지 않으므로 저장 수 내림차순(popularRouteIds 순서)대로 다시 정렬합니다.
        Map<Long, ActualRoute> routeById = new LinkedHashMap<>();
        actualRouteRepository.findByActualRouteIdInAndIsPublicTrueAndIsDeletedFalse(popularRouteIds)
                .forEach(route -> routeById.put(route.getActualRouteId(), route));

        return popularRouteIds.stream()
                .map(routeById::get)
                .filter(route -> route != null)
                .map(route -> {
                    List<ActualRouteSpot> spots = actualRouteSpotRepository
                            .findByActualRouteIdOrderByVisitOrderAsc(route.getActualRouteId());
                    Map<Long, TouristSpot> spotById = touristSpotRepository
                            .findAllById(spots.stream().map(ActualRouteSpot::getSpotId).toList())
                            .stream()
                            .collect(java.util.stream.Collectors.toMap(TouristSpot::getSpotId, s -> s));

                    List<String> stopNames = spots.stream()
                            .map(s -> spotById.get(s.getSpotId()))
                            .filter(s -> s != null)
                            .map(TouristSpot::getName)
                            .toList();
                    String photoUrl = spots.stream()
                            .map(s -> spotById.get(s.getSpotId()))
                            .filter(s -> s != null)
                            .map(TouristSpot::getImageUrl)
                            .filter(url -> url != null && !url.isBlank())
                            .findFirst()
                            .orElse(null);

                    // 핑 등록 시점 실제 GPS 좌표. 핑을 안 찍은 스팟은 null이라 여기서 걸러짐 -> 프론트 지도/경로 표시용
                    List<CoordinateResponse> coordinates = spots.stream()
                            .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                            .map(s -> CoordinateResponse.builder()
                                    .latitude(s.getLatitude().doubleValue())
                                    .longitude(s.getLongitude().doubleValue())
                                    .build())
                            .toList();

                    return PopularTripResponse.from(
                            route,
                            findNickname(route.getUserId()),
                            savedRouteRepository.countRecentSavesByRouteId(route.getActualRouteId(), since),
                            stopNames,
                            photoUrl,
                            spots.size(),
                            coordinates);
                })
                .toList();
    }

    /** 지금은 "week"만 지원합니다. 다른 period(예: month, all)가 필요해지면 여기만 확장하면 됩니다. */
    private int resolveWindowDays(String period) {
        return WEEK_WINDOW_DAYS;
    }

    private String findNickname(Long userId) {
        return userRepository.findById(userId)
                .map(AppUser::getNickname)
                .orElse(UNKNOWN_NICKNAME);
    }

    public TripDetailResponse getTripDetail(Long routeId) {
        ActualRoute route = actualRouteRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 루트입니다. id=" + routeId));

        List<ActualRouteSpot> spots = actualRouteSpotRepository
                .findByActualRouteIdOrderByVisitOrderAsc(routeId);
        Map<Long, TouristSpot> spotById = touristSpotRepository
                .findAllById(spots.stream().map(ActualRouteSpot::getSpotId).toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(TouristSpot::getSpotId, s -> s));

        // 방문 순서대로 각 장소의 평균 별점 + 등록된 루트 수까지 같이 계산
        List<StopSummaryResponse> stops = spots.stream()
                .map(s -> spotById.get(s.getSpotId()))
                .filter(s -> s != null)
                .map(spot -> StopSummaryResponse.builder()
                        .spotId(spot.getSpotId())
                        .name(spot.getName())
                        .averageRating(actualRouteSpotRepository.findAverageRatingBySpotId(spot.getSpotId()))
                        .registeredRouteCount(actualRouteSpotRepository.countDistinctRoutesBySpotId(spot.getSpotId()))
                        .build())
                .toList();

        String photoUrl = spots.stream()
                .map(s -> spotById.get(s.getSpotId()))
                .filter(s -> s != null)
                .map(TouristSpot::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(null);

        List<CoordinateResponse> coordinates = spots.stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(s -> CoordinateResponse.builder()
                        .latitude(s.getLatitude().doubleValue())
                        .longitude(s.getLongitude().doubleValue())
                        .build())
                .toList();

        AppUser writer = userRepository.findById(route.getUserId()).orElse(null);

        // ⚠️ 전체 기간 저장 수 세는 메서드가 따로 없어서, 기존 "최근 N일" 메서드를
        // 넓은 기간(100년 전부터)으로 부르는 편법이에요. 진짜 전체 카운트 메서드 있으면 교체해주세요.
        LocalDateTime since = LocalDateTime.now().minusYears(100);
        long savedCount = savedRouteRepository.countRecentSavesByRouteId(routeId, since);

        return TripDetailResponse.builder()
                .routeId(route.getActualRouteId())
                .writerNickname(writer != null ? writer.getNickname() : UNKNOWN_NICKNAME)
                .writerProfileImage(writer != null ? writer.getProfileImage() : null)
                .writerLevel(writer != null && writer.getLevel() != null ? writer.getLevel().name() : null)
                .stops(stops)
                .placeCount(stops.size())
                .photoUrl(photoUrl)
                .savedCount(savedCount)
                .coordinates(coordinates)
                .build();
    }
}
