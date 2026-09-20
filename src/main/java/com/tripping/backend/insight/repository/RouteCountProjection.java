package com.tripping.backend.insight.repository;

// findTopRoutes() 네이티브 쿼리 결과를 매핑하는 프로젝션. 컬럼 별칭(routeName, visitCount)과
// getter 이름이 일치해야 스프링 데이터가 자동으로 값을 채워준다.
public interface RouteCountProjection {
    String getRouteName();
    Long getVisitCount();
    // 방문 순서대로 이어붙인 spot_id 목록, 쉼표로 구분된 문자열 (예: "12,45,7"). 대체 관광지
    // 추천 등에서 이 루트를 이루는 실제 장소를 다시 찾아가야 할 때 쓴다.
    String getSpotIds();
}
