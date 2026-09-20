package com.tripping.backend.insight.repository;

import com.tripping.backend.entity.ActualRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

// B2B 대시보드 화면 전용 집계. InsightRouteRepository와 같은 조인 패턴(actual_route +
// actual_route_spot + tourist_spot + region)을 쓰지만, 트렌드 화면과 관심사가 달라서
// (여행객 수/루트 수/만족도/지역 랭킹/이동 네트워크) 레포지토리를 따로 둔다.
//
// 기간·지역 필터 기준은 트렌드 화면과 완전히 동일하다 — ar.travel_date BETWEEN start AND end,
// regionName이 null이면 지역 필터 없음. 같은 기간을 보면 두 화면의 "총 방문 핑"이 일치한다.
public interface DashboardRepository extends JpaRepository<ActualRoute, Long> {

    // "활성 여행객" 카드 - 기간(+지역) 안에 실제로 여행 기록을 남긴 서로 다른 사용자 수.
    @Query(value = """
            SELECT COUNT(DISTINCT ar.user_id)
            FROM actual_route ar
            JOIN actual_route_spot ars ON ars.actual_route_id = ar.actual_route_id
            JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
            LEFT JOIN region r ON r.region_id = ts.region_id
            WHERE ar.is_deleted = false
              AND ar.travel_date BETWEEN :start AND :end
              AND (:regionName IS NULL OR r.region_name = CAST(:regionName AS varchar))
            """, nativeQuery = true)
    Long countActiveTravelers(@Param("start") LocalDate start,
                              @Param("end") LocalDate end,
                              @Param("regionName") String regionName);

    // "기록된 루트" 카드 - 기간(+지역) 안에 기록된 실제 여행(actual_route) 건수.
    // 방문 핑(스팟 체크인)이 아니라 "여행" 단위라서 총 방문 핑보다 항상 작거나 같다.
    @Query(value = """
            SELECT COUNT(DISTINCT ar.actual_route_id)
            FROM actual_route ar
            JOIN actual_route_spot ars ON ars.actual_route_id = ar.actual_route_id
            JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
            LEFT JOIN region r ON r.region_id = ts.region_id
            WHERE ar.is_deleted = false
              AND ar.travel_date BETWEEN :start AND :end
              AND (:regionName IS NULL OR r.region_name = CAST(:regionName AS varchar))
            """, nativeQuery = true)
    Long countRoutes(@Param("start") LocalDate start,
                     @Param("end") LocalDate end,
                     @Param("regionName") String regionName);

    // "평균 만족도" 카드 - 해당 기간·지역의 방문 핑에 실제로 남겨진 ping_log 평점의 평균.
    // 평점이 하나도 없으면 averageRating이 null로 온다(서비스에서 null 처리).
    @Query(value = """
            SELECT AVG(pl.rating) AS averageRating, COUNT(pl.rating) AS ratingCount
            FROM ping_log pl
            JOIN actual_route_spot ars ON ars.actual_route_spot_id = pl.actual_route_spot_id
            JOIN actual_route ar ON ar.actual_route_id = ars.actual_route_id
            JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
            LEFT JOIN region r ON r.region_id = ts.region_id
            WHERE pl.is_deleted = false
              AND pl.rating IS NOT NULL
              AND ar.is_deleted = false
              AND ar.travel_date BETWEEN :start AND :end
              AND (:regionName IS NULL OR r.region_name = CAST(:regionName AS varchar))
            """, nativeQuery = true)
    RatingRow findAverageRating(@Param("start") LocalDate start,
                                @Param("end") LocalDate end,
                                @Param("regionName") String regionName);

    // "지역별 인기" 패널 - 우리 자체 방문 핑을 시도별로 집계한다. 지역 필터는 걸지 않는다
    // (지역별 비교가 목적이라 전체를 다 봐야 함). 핑이 없는 지역은 행 자체가 안 나오므로
    // 서비스에서 region 목록과 합쳐서 0으로 채운다.
    @Query(value = """
            SELECT r.region_name AS regionName,
                   r.api_area_cd AS areaCd,
                   COUNT(DISTINCT ars.actual_route_spot_id) AS visitCount
            FROM actual_route ar
            JOIN actual_route_spot ars ON ars.actual_route_id = ar.actual_route_id
            JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
            JOIN region r ON r.region_id = ts.region_id
            WHERE ar.is_deleted = false
              AND ar.travel_date BETWEEN :start AND :end
            GROUP BY r.region_name, r.api_area_cd
            """, nativeQuery = true)
    List<RegionPingRow> countPingsByRegion(@Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    // 관광공사 지역코드(api_area_cd)가 매핑된 시도 목록. 지역별 인기 랭킹의 기준 목록으로 쓴다.
    // 시군구 레벨 행에는 api_area_cd가 없어서 자연히 걸러진다.
    @Query(value = """
            SELECT DISTINCT r.region_name AS regionName, r.api_area_cd AS areaCd
            FROM region r
            WHERE r.api_area_cd IS NOT NULL AND r.api_area_cd <> ''
            """, nativeQuery = true)
    List<RegionCodeRow> findMappedRegions();

    // [이동 네트워크 - 노드] 기간·지역 안에서 방문 핑이 찍힌 관광지와 그 좌표.
    // 좌표는 핑 시점의 실제 GPS(actual_route_spot) 평균을 우선 쓰고, 없으면 관광지
    // 등록 좌표(tourist_spot)로 대체한다.
    @Query(value = """
            SELECT ts.spot_id AS spotId,
                   ts.name AS name,
                   COALESCE(AVG(ars.latitude), MAX(ts.latitude)) AS latitude,
                   COALESCE(AVG(ars.longitude), MAX(ts.longitude)) AS longitude,
                   COUNT(DISTINCT ars.actual_route_spot_id) AS visitCount
            FROM actual_route ar
            JOIN actual_route_spot ars ON ars.actual_route_id = ar.actual_route_id
            JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
            LEFT JOIN region r ON r.region_id = ts.region_id
            WHERE ar.is_deleted = false
              AND ar.travel_date BETWEEN :start AND :end
              AND (:regionName IS NULL OR r.region_name = CAST(:regionName AS varchar))
            GROUP BY ts.spot_id, ts.name
            ORDER BY COUNT(DISTINCT ars.actual_route_spot_id) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<NetworkNodeRow> findNetworkNodes(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end,
                                          @Param("regionName") String regionName,
                                          @Param("limit") int limit);

    // [이동 네트워크 - 엣지] 같은 여행 안에서 "바로 다음 순서로" 이어서 방문한 관광지 쌍.
    // visit_order가 연속(n -> n+1)인 것만 연결로 본다. weight는 그 연결이 몇 번 나왔는지.
    @Query(value = """
            SELECT a.spot_id AS fromSpotId,
                   b.spot_id AS toSpotId,
                   COUNT(*) AS weight
            FROM actual_route_spot a
            JOIN actual_route_spot b
              ON b.actual_route_id = a.actual_route_id
             AND b.visit_order = a.visit_order + 1
            JOIN actual_route ar ON ar.actual_route_id = a.actual_route_id
            JOIN tourist_spot ts ON ts.spot_id = a.spot_id
            LEFT JOIN region r ON r.region_id = ts.region_id
            WHERE ar.is_deleted = false
              AND ar.travel_date BETWEEN :start AND :end
              AND (:regionName IS NULL OR r.region_name = CAST(:regionName AS varchar))
            GROUP BY a.spot_id, b.spot_id
            ORDER BY COUNT(*) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<NetworkEdgeRow> findNetworkEdges(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end,
                                          @Param("regionName") String regionName,
                                          @Param("limit") int limit);

    interface RatingRow {
        Double getAverageRating();
        Long getRatingCount();
    }

    interface RegionPingRow {
        String getRegionName();
        String getAreaCd();
        Long getVisitCount();
    }

    interface RegionCodeRow {
        String getRegionName();
        String getAreaCd();
    }

    interface NetworkNodeRow {
        Long getSpotId();
        String getName();
        Double getLatitude();
        Double getLongitude();
        Long getVisitCount();
    }

    interface NetworkEdgeRow {
        Long getFromSpotId();
        Long getToSpotId();
        Long getWeight();
    }
}
