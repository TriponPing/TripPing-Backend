package com.tripping.backend.ping.service;

import com.tripping.backend.community.repository.AppUserRepository;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.entity.RegionPing;
import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.ping.dto.RegionPingRequest;
import com.tripping.backend.ping.dto.RegionPingResponse;
import com.tripping.backend.ping.repository.PingTouristSpotRepository;
import com.tripping.backend.ping.repository.RegionPingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// 지역핑(내 거주 지역 장소 평점/후기) 등록.
// 장소(위치) 자체는 새로 만들지 않고 기존 "새 장소 등록" 흐름(TouristSpotService.createPlace / TouristSpot)을
// 프론트에서 그대로 재사용하고, 여기서는 이미 만들어진(또는 이미 DB에 있던) spotId를 받아
// "그 장소가 본인 거주 지역인지"만 검증한 뒤 평점/후기를 저장함.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionPingService {

    private final RegionPingRepository regionPingRepository;
    private final PingTouristSpotRepository touristSpotRepository;
    private final AppUserRepository appUserRepository;

    @Transactional
    public RegionPingResponse registerRegionPing(Long userId, RegionPingRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        TouristSpot spot = touristSpotRepository.findById(request.spotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."));

        if (user.getRegionId() == null || !user.getRegionId().equals(spot.getRegionId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 거주 지역의 장소에만 지역핑을 등록할 수 있습니다.");
        }

        RegionPing regionPing = RegionPing.builder()
                .spotId(spot.getSpotId())
                .userId(userId)
                .rating(request.rating())
                .reviewComment(request.reviewComment())
                .build();

        RegionPing saved = regionPingRepository.save(regionPing);
        return RegionPingResponse.of(saved, spot, user.getNickname());
    }
}
