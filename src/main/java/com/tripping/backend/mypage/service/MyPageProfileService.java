package com.tripping.backend.mypage.service;

import com.tripping.backend.auth.repository.UserRepository;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.mypage.dto.ProfileResponse;
import com.tripping.backend.mypage.dto.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageProfileService {

    private final UserRepository userRepository;

    // 프로필 조회 - GET /users/me
    public ProfileResponse getProfile(Long userId) {
        return ProfileResponse.from(findUser(userId));
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

        return ProfileResponse.from(user);
    }

    private AppUser findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
