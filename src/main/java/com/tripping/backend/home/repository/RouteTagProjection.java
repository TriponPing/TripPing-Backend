package com.tripping.backend.home.repository;

/**
 * 루트별로 후기에 달린 해시태그 목록을 조회한 결과 프로젝션.
 * 네이티브 쿼리 SELECT 절의 별칭(routeId, tagName)과 getter 이름이 매칭됩니다.
 */
public interface RouteTagProjection {
    Long getRouteId();

    String getTagName();
}
