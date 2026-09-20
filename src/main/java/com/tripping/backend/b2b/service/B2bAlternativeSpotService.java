package com.tripping.backend.b2b.service;

import com.tripping.backend.b2b.dto.AlternativeSpotResponse;
import com.tripping.backend.b2b.repository.B2bAlternativeSpotRepository;
import com.tripping.backend.entity.TouristSpot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

// 상품 일정의 한 구간을 바꿀 때 보여줄 대체 관광지를 고른다.
//
// 담당자가 카테고리(관광지·음식점 등)를 고르면 그 구간의 장소 주변에서
// 같은 카테고리의 관광지를 찾아 3~4개만 올린다.
@Slf4j
@RequiredArgsConstructor
@Service
public class B2bAlternativeSpotService {

    private static final int DEFAULT_LIMIT = 4;
    private static final int MAX_LIMIT = 10;
    private static final double DEFAULT_RADIUS_KM = 30;
    private static final double MAX_RADIUS_KM = 100;

    private final B2bAlternativeSpotRepository alternativeSpotRepository;

    @Transactional(readOnly = true)
    public List<AlternativeSpotResponse> findAlternatives(Long spotId,
                                                          String category,
                                                          Integer limit,
                                                          Double radiusKm) {
        TouristSpot origin = alternativeSpotRepository.findById(spotId)
                .orElseThrow(() -> new NoSuchElementException("관광지를 찾지 못했습니다."));

        // 좌표가 없으면 "주변"을 계산할 수 없다. 화면에서는 추천 없이
        // 기존 구간을 그대로 두면 되므로 에러 대신 빈 목록을 준다.
        if (origin.getLatitude() == null || origin.getLongitude() == null) {
            log.info("좌표 없는 관광지라 대체 추천을 건너뜀: spotId={}", spotId);
            return List.of();
        }

        return alternativeSpotRepository.findAlternatives(
                        spotId,
                        origin.getLatitude().doubleValue(),
                        origin.getLongitude().doubleValue(),
                        blankToNull(category),
                        clamp(radiusKm, DEFAULT_RADIUS_KM, MAX_RADIUS_KM),
                        (int) clamp(limit == null ? null : limit.doubleValue(),
                                DEFAULT_LIMIT, MAX_LIMIT))
                .stream()
                .map(AlternativeSpotResponse::from)
                .toList();
    }

    // 카테고리를 안 넘기면 전체에서 찾는다.
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private double clamp(Double value, double fallback, double max) {
        if (value == null || value <= 0) return fallback;
        return Math.min(value, max);
    }
}
