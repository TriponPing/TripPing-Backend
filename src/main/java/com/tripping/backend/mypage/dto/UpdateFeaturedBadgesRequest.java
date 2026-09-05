package com.tripping.backend.mypage.dto;

import java.util.List;

/**
 * 꺼낼 뱃지 수정 요청 - 지금 프로필에 노출하고 싶은 뱃지 code 전체 목록(교체)
 * PUT /users/me/badges/featured
 */
public record UpdateFeaturedBadgesRequest(
        List<String> featuredBadgeCodes
) {
}
