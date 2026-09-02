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
    @Query(value = """
        SELECT ars.actual_route_spot_id AS routePlaceId,
               ars.spot_id AS spotId,
               ts.name AS spotName,
               COALESCE(ars.latitude, ts.latitude) AS latitude,
               COALESCE(ars.longitude, ts.longitude) AS longitude,
               ars.visit_order AS visitOrder
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
    }
}