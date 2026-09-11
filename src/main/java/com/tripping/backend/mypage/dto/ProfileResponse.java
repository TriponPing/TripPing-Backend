package com.tripping.backend.mypage.dto;

import com.tripping.backend.entity.AppUser;
import com.tripping.backend.entity.UserLevel;

import java.time.LocalDateTime;

/**
 * 프로필 조회 / 수정 결과 응답
 * GET /users/me, PATCH /users/me
 *
 * 👈 수정: level은 AppUser에 저장된 값이 아니라, 호출부(MyPageProfileService)가 넘겨주는
 * 총 핑 개수로 그때그때 계산함(UserLevel.fromPingCount 참고) - 자세한 이유는 AppUser 주석 참고.
 */
public record ProfileResponse(
        Long userId,
        String email,
        String nickname,
        String profileImage,
        String regionId,
        String language,
        String level,
        LocalDateTime updatedAt
) {
    public static ProfileResponse from(AppUser user, int totalPingCount) {
        return new ProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage(),
                user.getRegionId(),
                user.getLanguage(),
                UserLevel.fromPingCount(totalPingCount).name(),
                user.getUpdatedAt()
        );
    }
}
