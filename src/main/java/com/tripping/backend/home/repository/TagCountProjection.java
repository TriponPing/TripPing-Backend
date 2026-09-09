package com.tripping.backend.home.repository;

/**
 * Tag(해시태그) 별 등장 횟수 집계 결과 프로젝션.
 * JPQL SELECT 절의 별칭(keyword, count)과 getter 이름이 매칭됩니다.
 */
public interface TagCountProjection {
    String getKeyword();

    Long getCount();
}
