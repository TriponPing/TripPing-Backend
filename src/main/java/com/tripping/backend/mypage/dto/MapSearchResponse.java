package com.tripping.backend.mypage.dto;

import java.time.LocalDate;

/**
 * 지도 검색 결과
 * GET /users/me/map/search?name={여행이름}  (스펙: /trips/search?name= 과 파라미터명 통일)
 *
 * ACTUAL_ROUTE에는 별도 '제목' 컬럼이 없어서, 여행에 포함된 관광지 이름(TOURIST_SPOT.name)에
 * 키워드가 포함되는지로 검색합니다. 실제로 여행 제목 필드가 생기면 그 필드로 교체하세요.
 */
public record MapSearchResponse(
        Long tripId,
        String type,   // DRAWN | SAVED
        LocalDate travelDate
) {
}
