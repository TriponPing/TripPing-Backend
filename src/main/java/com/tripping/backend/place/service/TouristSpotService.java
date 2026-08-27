package com.tripping.backend.place.service;

import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.place.dto.TouristSpotResponse;
import com.tripping.backend.place.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    private final TouristSpotRepository touristSpotRepository;

    // 지도 기반 반경 검색
    public List<TouristSpotResponse> searchByLocation(double lat, double lng, double radius) {
        return touristSpotRepository.findWithinRadius(lat, lng, radius).stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    // 카테고리별 조회 (관광지/맛집/카페)
    public List<TouristSpotResponse> getByCategory(String category) {
        return touristSpotRepository.findByCategory(category).stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    // 탐색 지도 필터 기능 (현재 지역 필터만 지원, 혼잡도/시간대/Ping수는 추후 반영 예정)
    public List<TouristSpotResponse> getByCategoryAndFilters(String category, String regionId) {
        List<TouristSpot> spots;

        if (regionId != null && !regionId.isBlank()) {
            spots = touristSpotRepository.findByCategoryAndRegionId(category, regionId);
        } else {
            spots = touristSpotRepository.findByCategory(category);
        }

        return spots.stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    // 장소 상세 조회
    public TouristSpotResponse getDetail(Long placeId) {
        return touristSpotRepository.findById(placeId)
                .map(TouristSpotResponse::new)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. id=" + placeId));
    }
}