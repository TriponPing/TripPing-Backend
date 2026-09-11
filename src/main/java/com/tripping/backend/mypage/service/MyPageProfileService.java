package com.tripping.backend.mypage.service;

import com.tripping.backend.auth.repository.UserRepository;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.mypage.dto.ProfileResponse;
import com.tripping.backend.mypage.dto.ProfileUpdateRequest;
import com.tripping.backend.mypage.repository.MyPageActualRouteRepository;
import com.tripping.backend.mypage.repository.MyPageActualRouteSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageProfileService {

    private final UserRepository userRepository;
    private final MyPageActualRouteRepository actualRouteRepository;
    private final MyPageActualRouteSpotRepository actualRouteSpotRepository;

    // 프로필 조회 - GET /users/me
    public ProfileResponse getProfile(Long userId) {
        return ProfileResponse.from(findUser(userId), countTotalPings(userId));
    }

    // 프로필 수정 - PATCH /users/me
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        AppUser user = findUser(userId);

        // null(혹은 빈 값)인 필드는 건드리지 않음 -> partial update
        if (StringUtils.hasText(request.nickname())) {
            user.setNickname(request.nickname());
        }
        if (request.profileImage() != null) {
            user.setProfileImage(request.profileImage());
        }
        if (StringUtils.hasText(request.regionId())) {
            user.setRegionId(request.regionId());
        }
        if (StringUtils.hasText(request.language())) {
            user.setLanguage(request.language());
        }
        // JPA 영속성 컨텍스트 dirty checking으로 UPDATE 반영됨 (별도 save 호출 불필요)

        return ProfileResponse.from(user, countTotalPings(userId));
    }

    // 👈 새로 추가: "Pinger" 레벨 계산용 - 이 유저가 다녀온 모든 여행에 찍은 핑(ACTUAL_ROUTE_SPOT)
    // 총 개수(중복/같은 장소 재방문도 각각 카운트 - 레벨은 "몇 번 찍었는지"라 다녀온 장소 개수랑은 다름)
    private int countTotalPings(Long userId) {
        List<Long> routeIds = actualRouteRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .map(ActualRoute::getActualRouteId)
                .toList();
        if (routeIds.isEmpty()) {
            return 0;
        }
        return actualRouteSpotRepository.findByActualRouteIdInOrderByActualRouteIdAscVisitOrderAsc(routeIds).size();
    }

    private AppUser findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
