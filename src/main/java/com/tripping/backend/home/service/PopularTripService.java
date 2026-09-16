package com.tripping.backend.home.service;

import com.tripping.backend.auth.repository.UserRepository;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.entity.UserLevel;
import com.tripping.backend.home.dto.response.CoordinateResponse;
import com.tripping.backend.home.dto.response.PopularTripResponse;
import com.tripping.backend.home.repository.HomeActualRouteRepository;
import com.tripping.backend.home.repository.HomeActualRouteSpotRepository;
import com.tripping.backend.home.repository.HomePingLogTagRepository;
import com.tripping.backend.home.repository.HomeSavedRouteRepository;
import com.tripping.backend.home.repository.HomeTouristSpotRepository;
import com.tripping.backend.home.repository.RouteTagProjection;
import com.tripping.backend.home.dto.response.TripDetailResponse;
import com.tripping.backend.home.dto.response.StopSummaryResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final HomePingLogTagRepository pingLogTagRepository;
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
                .map(route -> buildTripResponse(route, since))
                .toList();
    }

    // 이 키워드(해시태그)가 달린 후기가 있는 루트 목록. 탐색 > 인기 키워드 더보기에서 칩 선택 시 사용.
    public List<PopularTripResponse> getRoutesByKeyword(String keyword, int limit) {
        List<Long> routeIds = pingLogTagRepository.findActualRouteIdsByKeyword(keyword);
        if (routeIds.isEmpty()) {
            return List.of();
        }

        List<ActualRoute> routes = actualRouteRepository
                .findByActualRouteIdInAndIsPublicTrueAndIsDeletedFalse(routeIds).stream()
                .limit(limit)
                .toList();

        // savedCount 표시는 "이번주 인기 루트" 카드와 동일하게 최근 1주 기준으로 맞춤
        LocalDateTime since = LocalDateTime.now().minusDays(WEEK_WINDOW_DAYS);

        // 카드에 "#데이트 #바다"처럼 이 루트에 달린 태그 전부를 보여주기 위해 한 번에 조회
        List<Long> shownRouteIds = routes.stream().map(ActualRoute::getActualRouteId).toList();
        Map<Long, List<String>> tagsByRouteId = pingLogTagRepository.findTagsByRouteIds(shownRouteIds).stream()
                .collect(Collectors.groupingBy(
                        RouteTagProjection::getRouteId,
                        Collectors.mapping(RouteTagProjection::getTagName, Collectors.toList())));

        return routes.stream()
                .map(route -> buildTripResponse(route, since).toBuilder()
                        .tags(tagsByRouteId.getOrDefault(route.getActualRouteId(), List.of()))
                        .build())
                .toList();
    }

    // 👈 수정: private -> package-private. NearbyTripService("내 주변 코스" = 마지막 핑 주변 코스)에서
    // 루트 하나를 카드용 응답으로 만드는 로직을 그대로 재사용하기 위함(중복 구현 방지).
    PopularTripResponse buildTripResponse(ActualRoute route, LocalDateTime savedCountSince) {
        List<ActualRouteSpot> spots = actualRouteSpotRepository
                .findByActualRouteIdOrderByVisitOrderAsc(route.getActualRouteId());
        Map<Long, TouristSpot> spotById = touristSpotRepository
                .findAllById(spots.stream().map(ActualRouteSpot::getSpotId).toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(TouristSpot::getSpotId, s -> s));

        List<TouristSpot> touristSpots = spots.stream()
                .map(s -> spotById.get(s.getSpotId()))
                .filter(s -> s != null)
                .toList();

        List<String> stopNames = touristSpots.stream()
                .map(TouristSpot::getName)
                .toList();
        String photoUrl = touristSpots.stream()
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
                savedRouteRepository.countRecentSavesByRouteId(route.getActualRouteId(), savedCountSince),
                stopNames,
                photoUrl,
                spots.size(),
                coordinates,
                determineTheme(touristSpots));
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

    // TouristSpotService.determineTheme() / RouteRecommendService.determineTheme()랑 같은 로직
    // (카테고리 비율로 테마명 결정). 루트 자체엔 제목이 없어서 상세보기 제목으로 씀.
    private String determineTheme(List<TouristSpot> spots) {
        Map<String, Long> categoryCount = spots.stream()
                .collect(Collectors.groupingBy(TouristSpot::getCategory, Collectors.counting()));

        String topCategory = categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        long topCount = categoryCount.getOrDefault(topCategory, 0L);
        boolean isEvenlyMixed = categoryCount.size() > 1 && topCount <= spots.size() / 2.0;

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

        List<TouristSpot> touristSpots = spots.stream()
                .map(s -> spotById.get(s.getSpotId()))
                .filter(s -> s != null)
                .toList();

        return TripDetailResponse.builder()
                .routeId(route.getActualRouteId())
                .title(determineTheme(touristSpots))
                .writerNickname(writer != null ? writer.getNickname() : UNKNOWN_NICKNAME)
                .writerProfileImage(writer != null ? writer.getProfileImage() : null)
                // 👈 수정: level을 AppUser에서 직접 읽던 것 -> 총 핑 개수 기준으로 계산 (이유는 AppUser 주석 참고)
                .writerLevel(writer != null
                        ? UserLevel.fromPingCount((int) actualRouteSpotRepository.countByWriterUserId(writer.getUserId())).name()
                        : null)
                .stops(stops)
                .placeCount(stops.size())
                .photoUrl(photoUrl)
                .savedCount(savedCount)
                .coordinates(coordinates)
                .build();
    }
}
