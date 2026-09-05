package com.tripping.backend.mypage.dto;

/**
 * 뱃지 목록 조회/수정 응답 (설정 화면 "뱃지" / "꺼낼 뱃지")
 * GET /users/me/badges, PUT /users/me/badges/featured
 */
public record BadgeResponse(
        String code,
        String label,
        String emoji,
        boolean featured // true면 "꺼낼 뱃지"(마이페이지 프로필에 노출)에 포함됨
) {
}
