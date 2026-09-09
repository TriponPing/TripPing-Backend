package com.tripping.backend.home.repository;

import com.tripping.backend.entity.PingLogTag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ping 도메인에 이미 PingLogTagRepository(후기 등록/수정용)가 있지만,
 * 여기서는 "이번주 인기 키워드" 집계용 쿼리가 필요해서 home 도메인 전용으로 따로 둡니다.
 * (HomeSavedRouteRepository와 동일한 이유/패턴)
 */
public interface HomePingLogTagRepository extends JpaRepository<PingLogTag, Long> {

    /**
     * 기준 시각(since) 이후 작성된 후기에 달린 해시태그를 등장 빈도 내림차순으로 집계합니다.
     * 삭제된 후기(isDeleted=true)는 제외합니다. Pageable로 상위 N개만 잘라옵니다.
     */
    @Query("""
        SELECT t.name AS keyword, COUNT(plt) AS count
        FROM PingLogTag plt
        JOIN plt.tag t
        JOIN plt.pingLog pl
        WHERE pl.createdAt >= :since AND pl.isDeleted = false
        GROUP BY t.name
        ORDER BY COUNT(plt) DESC
        """)
    List<TagCountProjection> findPopularTags(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 이 키워드(해시태그)가 달린 후기가 하나라도 있는 루트의 actualRouteId 목록.
     * PingLog.actualRouteSpotId는 매핑된 연관관계가 아니라 순수 FK 컬럼이라, JPQL로는
     * 엔티티끼리 조인이 안 돼서 네이티브 쿼리로 직접 조인함.
     */
    @Query(value = """
        SELECT DISTINCT ars.actual_route_id
        FROM ping_log_tag plt
        JOIN tag t ON t.tag_id = plt.tag_id
        JOIN ping_log pl ON pl.ping_log_id = plt.ping_log_id
        JOIN actual_route_spot ars ON ars.actual_route_spot_id = pl.actual_route_spot_id
        WHERE t.name = :keyword AND pl.is_deleted = false
        """, nativeQuery = true)
    List<Long> findActualRouteIdsByKeyword(@Param("keyword") String keyword);

    /**
     * 주어진 루트들 각각에 달린 해시태그 전부(중복 제거) - 키워드로 찾은 루트 카드에
     * "#데이트 #바다"처럼 태그 칩을 보여주기 위함.
     */
    @Query(value = """
        SELECT DISTINCT ars.actual_route_id AS routeId, t.name AS tagName
        FROM ping_log_tag plt
        JOIN tag t ON t.tag_id = plt.tag_id
        JOIN ping_log pl ON pl.ping_log_id = plt.ping_log_id
        JOIN actual_route_spot ars ON ars.actual_route_spot_id = pl.actual_route_spot_id
        WHERE ars.actual_route_id IN (:routeIds) AND pl.is_deleted = false
        """, nativeQuery = true)
    List<RouteTagProjection> findTagsByRouteIds(@Param("routeIds") List<Long> routeIds);
}
