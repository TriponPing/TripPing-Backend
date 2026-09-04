package com.tripping.backend.home.repository;

/**
 * RouteCondition.theme 별 등장 횟수 집계 결과 프로젝션.
 * JPQL SELECT 절의 별칭(theme, count)과 getter 이름이 매칭됩니다.
 */
public interface ThemeCountProjection {
    String getTheme();

    Long getCount();
}
