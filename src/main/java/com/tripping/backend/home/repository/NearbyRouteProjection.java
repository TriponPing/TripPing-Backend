package com.tripping.backend.home.repository;

/**
 * 네이티브 쿼리(하버사인 거리 계산) 결과를 담는 프로젝션.
 * SELECT 절의 컬럼 별칭(actualRouteId, distanceKm)과 getter 이름이 매칭됩니다.
 */
public interface NearbyRouteProjection {
    Long getActualRouteId();

    Double getDistanceKm();
}
