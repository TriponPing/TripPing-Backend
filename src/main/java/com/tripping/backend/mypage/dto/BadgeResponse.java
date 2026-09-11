package com.tripping.backend.mypage.dto;

/**
 * 뱃지 목록 조회/수정 응답 (설정 화면 "뱃지" / "꺼낼 뱃지")
 * GET /users/me/badges, PUT /users/me/badges/featured
 */
public record BadgeResponse(
        String code,
        String label,
        String emoji,
        String conditionDesc, // 달성 조건 설명 (예: "여행 1개를 완주해보세요") - 안 딴 뱃지에 잠금 상태로 노출
        boolean earned,       // true면 실제로 달성한 뱃지 (UserLevel과 같은 방식으로 매번 실제 활동 데이터 기준 계산)
        boolean featured      // true면 "꺼낼 뱃지"(마이페이지 프로필에 노출)에 포함됨 - earned=false인 뱃지는 절대 true일 수 없음
) {
}
