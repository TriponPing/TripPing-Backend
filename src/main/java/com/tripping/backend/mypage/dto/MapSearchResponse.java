package com.tripping.backend.mypage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지도 검색 결과
 * GET /users/me/map/search?keyword={여행이름}  (루트 이름 검색 등 다른 검색 API와 파라미터명 통일)
 *
 * ACTUAL_ROUTE에는 별도 '제목' 컬럼이 없어서, 여행에 포함된 관광지 이름(TOURIST_SPOT.name)에
 * 키워드가 포함되는지로 검색합니다. 실제로 여행 제목 필드가 생기면 그 필드로 교체하세요.
 *
 * 👈 새로 추가: spotName/latitude/longitude - 프론트에서 검색 결과에 이름을 표시하고(기존엔
 * travelDate만 내려줘서 뭐가 매칭됐는지 알 수 없었음), 결과를 눌렀을 때 바로 그 좌표로 지도를
 * 이동시킬 수 있게 함(기존엔 별도 목록에서 다시 찾아야 했음). 이 여행의 "대표 스팟"(방문순서 1번)
 * 이름/좌표라서 실제로 키워드에 매칭된 스팟과 다를 수 있음 - 이 앱에서 "여행 이름" 대신 쓰는
 * 대표 스팟 표기 관례(TripSummaryResponse.representativeSpotName 등)와 통일한 것.
 */
public record MapSearchResponse(
        Long tripId,
        String type,   // DRAWN | SAVED
        LocalDate travelDate,
        String spotName,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
