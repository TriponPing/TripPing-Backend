package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 대체 관광지 추천용 조회.
//
// 기준 장소 주변에서 같은 카테고리의 관광지를 찾고, 핑 개수(방문자 수)와
// 평점을 함께 집계한다. 기능별로 TouristSpot 레포지토리를 따로 두는
// 이 프로젝트의 관례를 따라 검색용(B2bTouristSpotRepository)과 분리했다.
public interface B2bAlternativeSpotRepository extends JpaRepository<TouristSpot, Long> {

    // 정렬은 방문자 수 → 평점 → 거리 순이다.
    // 핑이 아직 쌓이지 않아 방문자 수가 모두 0이고 평점이 전부 NULL인 동안은
    // 세 번째 기준만 살아남아 자연히 "가까운 곳 순"이 된다. 데이터가 들어오면
    // 쿼리를 고치지 않아도 앞의 두 기준이 그대로 동작한다.
    @Query(value = """
        SELECT t.spot_id       AS spot_id,
               t.name          AS name,
               t.category      AS category,
               t.address       AS address,
               t.latitude      AS latitude,
               t.longitude     AS longitude,
               t.image_url     AS image_url,
               t.visit_count   AS visit_count,
               t.average_rating AS average_rating,
               t.distance_km   AS distance_km
        FROM (
            SELECT s.spot_id,
                   s.name,
                   s.category,
                   s.address,
                   s.latitude,
                   s.longitude,
                   s.image_url,
                   COUNT(pl.ping_log_id) AS visit_count,
                   AVG(pl.rating)        AS average_rating,
                   6371 * acos(LEAST(1, GREATEST(-1,
                       cos(radians(:latitude)) * cos(radians(CAST(s.latitude AS double precision)))
                       * cos(radians(CAST(s.longitude AS double precision)) - radians(:longitude))
                       + sin(radians(:latitude)) * sin(radians(CAST(s.latitude AS double precision)))
                   ))) AS distance_km
            FROM tourist_spot s
            LEFT JOIN actual_route_spot ars ON ars.spot_id = s.spot_id
            LEFT JOIN ping_log pl ON pl.actual_route_spot_id = ars.actual_route_spot_id
                                 AND pl.is_deleted = false
            WHERE s.spot_id <> :spotId
              AND s.latitude IS NOT NULL
              AND s.longitude IS NOT NULL
              AND (CAST(:category AS varchar) IS NULL OR s.category = CAST(:category AS varchar))
            GROUP BY s.spot_id, s.name, s.category, s.address,
                     s.latitude, s.longitude, s.image_url
        ) t
        WHERE t.distance_km <= :radiusKm
        ORDER BY t.visit_count DESC,
                 t.average_rating DESC NULLS LAST,
                 t.distance_km ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<AlternativeSpotRow> findAlternatives(@Param("spotId") Long spotId,
                                              @Param("latitude") double latitude,
                                              @Param("longitude") double longitude,
                                              @Param("category") String category,
                                              @Param("radiusKm") double radiusKm,
                                              @Param("limit") int limit);

    interface AlternativeSpotRow {
        Long getSpotId();

        String getName();

        String getCategory();

        String getAddress();

        java.math.BigDecimal getLatitude();

        java.math.BigDecimal getLongitude();

        String getImageUrl();

        long getVisitCount();

        Double getAverageRating();

        double getDistanceKm();
    }
}
