package com.tripping.backend.place.service;

import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.ping.dto.SpotPingStatsResponse;
import com.tripping.backend.ping.service.PingService;
import com.tripping.backend.place.dto.CreatePlaceRequest;
import com.tripping.backend.place.dto.TouristSpotResponse;
import com.tripping.backend.place.repository.PlaceTouristSpotRepository;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.home.repository.HomeActualRouteSpotRepository;
import com.tripping.backend.place.dto.SpotDetailResponse;
import com.tripping.backend.place.dto.RegisteredRouteCardResponse;
import com.tripping.backend.place.dto.SpotReviewResponse;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    private final PlaceTouristSpotRepository touristSpotRepository;
    private final HomeActualRouteSpotRepository actualRouteSpotRepository;
    private final PingService pingService;
    private final com.tripping.backend.home.repository.HomeSavedRouteRepository savedRouteRepository;
    private final TourApiService tourApiService;

    public List<TouristSpotResponse> searchByLocation(double lat, double lng, double radius) {
        return touristSpotRepository.findWithinRadius(lat, lng, radius).stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    public List<TouristSpotResponse> getByCategory(String category) {
        return touristSpotRepository.findByCategory(category).stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    // 탐색 지도 필터 기능 (지역, 시간대, 핑개수 조건 지원)
    public List<TouristSpotResponse> getByCategoryAndFilters(
            String category,
            String regionId,
            String timeSlot,
            Integer minPingCount
    ) {
        List<TouristSpot> spots;

        if (regionId != null && !regionId.isBlank()) {
            // "동네핑거가 등록한" 화면이라, 장소 자체 지역이 아니라 "등록한 사람의 거주 지역+주민핑거 여부"로 필터링
            spots = touristSpotRepository.findByCategoryAndCreatorRegionAndResidentPinger(category, regionId);
        } else {
            spots = touristSpotRepository.findByCategory(category);
        }

        if (timeSlot != null || minPingCount != null) {
            spots = spots.stream()
                    .filter(spot -> matchesPingFilter(spot.getSpotId(), timeSlot, minPingCount))
                    .toList();
        }

        return spots.stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    private boolean matchesPingFilter(Long spotId, String timeSlot, Integer minPingCount) {
        SpotPingStatsResponse stats = pingService.getSpotPingStats(spotId);

        if (timeSlot != null && !timeSlot.equals(stats.popularTimeSlot())) {
            return false;
        }

        if (minPingCount != null && stats.totalPingCount() < minPingCount) {
            return false;
        }

        return true;
    }

    public TouristSpotResponse getDetail(Long placeId) {
        return touristSpotRepository.findById(placeId)
                .map(TouristSpotResponse::new)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. id=" + placeId));
    }

    public List<TouristSpotResponse> searchByKeyword(String query, String regionId) {
        List<TouristSpot> spots = (regionId != null && !regionId.isBlank())
                ? touristSpotRepository.findByRegionIdAndNameContainingIgnoreCase(regionId, query)
                : touristSpotRepository.findByNameContainingIgnoreCase(query);

        return spots.stream()
                .map(spot -> {
                    SpotPingStatsResponse stats = pingService.getSpotPingStats(spot.getSpotId());
                    return new TouristSpotResponse(spot, stats.popularTimeSlot(), stats.totalPingCount());
                })
                .toList();
    }

    public SpotDetailResponse getSpotDetail(Long spotId) {
        TouristSpot spot = touristSpotRepository.findById(spotId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. id=" + spotId));

        SpotPingStatsResponse stats = pingService.getSpotPingStats(spotId);

        List<Long> routeIds = actualRouteSpotRepository.findPublicRouteIdsBySpotId(spotId);

        // 저장(찜) 수를 미리 다 계산해두고, 저장 많은 순으로 정렬해서 상위 3개만
        Map<Long, Long> savedCountByRouteId = routeIds.stream()
                .collect(java.util.stream.Collectors.toMap(id -> id, savedRouteRepository::countByActualRouteId));

        List<RegisteredRouteCardResponse> registeredRoutes = routeIds.stream()
                .sorted(java.util.Comparator.comparingLong((Long id) -> savedCountByRouteId.getOrDefault(id, 0L)).reversed())
                .limit(3)
                .map(this::buildRouteCard)
                .toList();

        List<HomeActualRouteSpotRepository.SpotReviewProjection> reviewRows =
                actualRouteSpotRepository.findReviewsBySpotId(spotId);
        List<SpotReviewResponse> reviews = reviewRows.stream()
                .map(r -> new SpotReviewResponse(
                        r.getWriterNickname(),
                        r.getRating(),
                        r.getReviewComment(),
                        r.getPhotoUrl(),
                        r.getCreatedAt()
                ))
                .toList();

        // 장소 상세 화면(로그 상세보기 등)에 표시할 평균 별점/리뷰 개수 - 별점 있는 후기 기준
        List<Integer> ratings = reviews.stream()
                .map(SpotReviewResponse::getRating)
                .filter(r -> r != null)
                .toList();
        Double averageRating = ratings.isEmpty()
                ? null
                : ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        return SpotDetailResponse.builder()
                .spotId(spot.getSpotId())
                .name(spot.getName())
                .address(spot.getAddress())
                .category(spot.getCategory())
                .imageUrl(spot.getImageUrl())
                .description(spot.getDescription())
                .pingCount(stats.totalPingCount())
                .popularTimeSlot(stats.popularTimeSlot())
                .registeredRoutes(registeredRoutes)
                .reviews(reviews)
                .averageRating(averageRating)
                .reviewCount(reviews.size())
                .build();
    }

    private RegisteredRouteCardResponse buildRouteCard(Long routeId) {
        List<ActualRouteSpot> spots = actualRouteSpotRepository.findByActualRouteIdOrderByVisitOrderAsc(routeId);
        Map<Long, TouristSpot> spotById = touristSpotRepository
                .findAllById(spots.stream().map(ActualRouteSpot::getSpotId).toList())
                .stream()
                .collect(Collectors.toMap(TouristSpot::getSpotId, s -> s));

        List<TouristSpot> touristSpots = spots.stream()
                .map(s -> spotById.get(s.getSpotId()))
                .filter(s -> s != null)
                .toList();

        String theme = determineTheme(touristSpots);
        String photoUrl = touristSpots.stream()
                .map(TouristSpot::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(null);

        return RegisteredRouteCardResponse.builder()
                .actualRouteId(routeId)
                .themeName(theme)
                .photoUrl(photoUrl)
                .placeCount(touristSpots.size())
                .build();
    }

    // RouteRecommendService.determineTheme()랑 같은 로직 (카테고리 비율로 테마명 결정)
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

    // 👈 새로 추가: 새 장소 등록 - POST /places
    // 네이버맵 POI 등 아직 우리 DB에 없는 장소를 클라이언트가 새로 등록할 때 씀
    // ⚠️ TouristSpot.builder() 구성은 SavedPlace 엔티티의 빌더 패턴을 보고 추측했습니다.
    // 실제 TouristSpot 엔티티 파일 보여주시면 정확히 맞춰드릴게요.
    @Transactional
    public TouristSpotResponse createPlace(Long userId, CreatePlaceRequest request) {
        TouristSpot spot = TouristSpot.builder()
                .name(request.name())
                .category(request.category())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .regionId(request.regionId())
                .createdByUserId(userId)
                .build();

        // TourAPI에서 이름으로 검색해서 설명 자동으로 채워넣기 (실패해도 등록 자체는 계속 진행됨)
        try {
            String contentId = tourApiService.findContentId(request.name());
            if (contentId != null) {
                String overview = tourApiService.fetchOverview(contentId);
                if (overview != null) {
                    spot.setApiContentId(contentId);
                    spot.setDescription(overview);
                }
            }
        } catch (Exception e) {
            // TourAPI 실패해도 장소 등록 자체는 막지 않음. 나중에 백필로 다시 채울 수 있음.
        }

        TouristSpot saved = touristSpotRepository.save(spot);
        return new TouristSpotResponse(saved);
    }

    // 이름으로 TourAPI에서 검색 -> contentId 찾고 -> 설명(overview) 가져와서 description에 채워넣음.
    // 한 번에 너무 많이 부르면 하루 호출 한도(보통 1000회) 넘을 수 있어서 limit으로 나눠서 실행.
    @Transactional
    public int backfillDescriptionsFromTourApi(int limit) {
        List<TouristSpot> targets = touristSpotRepository
                .findByDescriptionIsNull(org.springframework.data.domain.PageRequest.of(0, limit));

        int updatedCount = 0;
        for (TouristSpot spot : targets) {
            String contentId = tourApiService.findContentId(spot.getName());
            if (contentId == null) {
                continue;
            }
            String overview = tourApiService.fetchOverview(contentId);
            if (overview == null) {
                continue;
            }
            spot.setApiContentId(contentId);
            spot.setDescription(overview);
            touristSpotRepository.save(spot);
            updatedCount++;
        }
        return updatedCount;
    }

}