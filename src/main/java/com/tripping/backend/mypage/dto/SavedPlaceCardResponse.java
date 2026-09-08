package com.tripping.backend.mypage.dto;

/**
 * 저장한 장소(북마크한 단일 스팟) 목록 조회
 * GET /users/me/places/saved
 */
public record SavedPlaceCardResponse(
        Long spotId,            // 저장 취소 시 DELETE /places/{spotId}/saved/me 에 사용
        String name,
        String category,
        String imageUrl,
        long pingCount,         // 이 장소에 남겨진 총 핑 개수
        long savedCount         // 이 장소를 저장한 전체 유저 수(전체 기간)
) {
}
