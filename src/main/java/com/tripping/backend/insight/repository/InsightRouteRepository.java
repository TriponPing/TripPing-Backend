package com.tripping.backend.insight.repository;

import com.tripping.backend.entity.ActualRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

// B2B 인사이트(트렌드/급상승 루트) 화면 전용 조회. ActualRoute 자체를 조작하는 게 아니라
// ActualRoute+ActualRouteSpot+TouristSpot+Region을 묶어서 집계만 하므로, 쓰기 메서드는 없고
// JpaRepository는 편의상 얹어둔 것 (실제로 사용하는 건 아래 네이티브 쿼리 2개뿐).
public interface InsightRouteRepository extends JpaRepository<ActualRoute, Long> {

    // 한 번의 실제 여행(actual_route)에서 방문한 장소들을 방문 순서(visit_order)대로 이어붙여
    // "성산일출봉 → 섭지코지 → 우도" 같은 하나의 "루트 이름"을 만들고, 그 조합이 기간 내에
    // 몇 번이나 나왔는지 세서 상위 10개를 돌려준다. regionName이 null이면 지역 필터 없음.
    @Query(value = """
            SELECT route_name AS routeName, COUNT(*) AS visitCount
            FROM (
                SELECT ars.actual_route_id,
                       STRING_AGG(ts.name, ' → ' ORDER BY ars.visit_order) AS route_name,
                       BOOL_AND(:regionName IS NULL OR r.region_name = CAST(:regionName AS varchar)) AS region_match
                FROM actual_route ar
                JOIN actual_route_spot ars ON ars.actual_route_id = ar.actual_route_id
                JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
                LEFT JOIN region r ON r.region_id = ts.region_id
                WHERE ar.is_deleted = false
                  AND ar.travel_date BETWEEN :start AND :end
                GROUP BY ars.actual_route_id
            ) grouped
            WHERE region_match
            GROUP BY route_name
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<RouteCountProjection> findTopRoutes(@Param("start") LocalDate start,
                                              @Param("end") LocalDate end,
                                              @Param("regionName") String regionName);

    // "총 방문 핑" 카드용 - 기간(+지역) 내 실제로 방문 기록된 장소(핑) 개수.
    @Query(value = """
            SELECT COUNT(DISTINCT ars.actual_route_spot_id)
            FROM actual_route ar
            JOIN actual_route_spot ars ON ars.actual_route_id = ar.actual_route_id
            JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
            LEFT JOIN region r ON r.region_id = ts.region_id
            WHERE ar.is_deleted = false
              AND ar.travel_date BETWEEN :start AND :end
              AND (:regionName IS NULL OR r.region_name = CAST(:regionName AS varchar))
            """, nativeQuery = true)
    Long countVisits(@Param("start") LocalDate start,
                      @Param("end") LocalDate end,
                      @Param("regionName") String regionName);
}
