package com.tripping.backend.trip.repository;

import com.tripping.backend.entity.ActualRouteSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TripActualRouteSpotRepository extends JpaRepository<ActualRouteSpot, Long> {

    List<ActualRouteSpot> findByActualRouteId(Long actualRouteId);

    // 루트 하나에 속한 장소들을, 지도에 찍을 좌표+이름과 함께 조회 (방문 순서대로)
    // 👈 수정: visitTime도 같이 내려서, 홈 위젯에서 "이미 찍은 핑(파란색)"과 "다음 찍을 핑(포커스)"을
    // 구분할 수 있게 함 (visitTime이 null이 아니면 이미 confirm된 핑).
    @Query(value = """
        SELECT ars.actual_route_spot_id AS routePlaceId,
               ars.spot_id AS spotId,
               ts.name AS spotName,
               COALESCE(ars.latitude, ts.latitude) AS latitude,
               COALESCE(ars.longitude, ts.longitude) AS longitude,
               ars.visit_order AS visitOrder,
               ars.visit_time AS visitTime
        FROM actual_route_spot ars
        JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
        WHERE ars.actual_route_id = :actualRouteId
        ORDER BY ars.visit_order ASC
        """, nativeQuery = true)
    List<TripRouteMapSpotProjection> findMapSpotsByActualRouteId(@Param("actualRouteId") Long actualRouteId);

    interface TripRouteMapSpotProjection {
        Long getRoutePlaceId();
        Long getSpotId();
        String getSpotName();
        BigDecimal getLatitude();
        BigDecimal getLongitude();
        Integer getVisitOrder();
        java.time.LocalDateTime getVisitTime();
    }
}