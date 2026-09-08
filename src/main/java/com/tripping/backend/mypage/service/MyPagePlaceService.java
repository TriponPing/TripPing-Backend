package com.tripping.backend.mypage.service;

import com.tripping.backend.entity.SavedPlace;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.mypage.dto.PageResponse;
import com.tripping.backend.mypage.dto.SavedPlaceCardResponse;
import com.tripping.backend.mypage.repository.MyPageSavedPlaceRepository;
import com.tripping.backend.mypage.repository.MyPageTouristSpotRepository;
import com.tripping.backend.ping.service.PingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPagePlaceService {

    private final MyPageSavedPlaceRepository savedPlaceRepository;
    private final MyPageTouristSpotRepository touristSpotRepository;
    private final PingService pingService;

    // 저장한 장소(북마크한 단일 스팟) 목록 조회 - GET /users/me/places/saved
    // 장소 북마크 자체는 place 도메인(SavedPlaceService)에서 이미 처리 중 - 여기서는 마이페이지용 목록 조회만 담당
    public PageResponse<SavedPlaceCardResponse> getSavedPlaces(Long userId, Pageable pageable) {
        Page<SavedPlace> page = savedPlaceRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Long> spotIds = page.getContent().stream()
                .map(SavedPlace::getSpotId)
                .toList();

        Map<Long, TouristSpot> spotById = spotIds.isEmpty()
                ? Map.of()
                : touristSpotRepository.findAllById(spotIds).stream()
                        .collect(Collectors.toMap(TouristSpot::getSpotId, s -> s));

        List<SavedPlaceCardResponse> content = page.getContent().stream()
                .map(sp -> {
                    TouristSpot spot = spotById.get(sp.getSpotId());
                    long pingCount = pingService.getSpotPingStats(sp.getSpotId()).totalPingCount();
                    long savedCount = savedPlaceRepository.countBySpotId(sp.getSpotId());
                    return new SavedPlaceCardResponse(
                            sp.getSpotId(),
                            spot != null ? spot.getName() : null,
                            spot != null ? spot.getCategory() : null,
                            spot != null ? spot.getImageUrl() : null,
                            pingCount,
                            savedCount
                    );
                })
                .toList();

        return PageResponse.of(content, page);
    }
}
