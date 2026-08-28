package com.tripping.backend.mypage.dto;

import com.tripping.backend.entity.AppUser;

import java.time.LocalDateTime;

/**
 * 프로필 조회 / 수정 결과 응답
 * GET /users/me, PATCH /users/me
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
    public static ProfileResponse from(AppUser user) {
        return new ProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage(),
                user.getRegionId(),
                user.getLanguage(),
                user.getLevel() != null ? user.getLevel().name() : null,
                user.getUpdatedAt()
        );
    }
}
