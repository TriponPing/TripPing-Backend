package com.tripping.backend.trip.repository;

import com.tripping.backend.entity.ActualRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional; // ⭐️ 임포트 추가

public interface TripActualRouteRepository extends JpaRepository<ActualRoute, Long> {

    // ⭐️ [추가] 유저의 진행 중(IN_PROGRESS)인 최신 여행 조회
    @Query("SELECT r FROM ActualRoute r WHERE r.userId = :userId AND r.status = 'IN_PROGRESS' AND r.isDeleted = false ORDER BY r.actualRouteId DESC")
    Optional<ActualRoute> findFirstInProgressRoute(@Param("userId") Long userId);

    // 지도용 루트 검색: 공개(isPublic) + 삭제안됨(isDeleted=false) + 지역/카테고리 조건에 맞는 루트 id 목록
    @Query(value = """
        SELECT DISTINCT ar.actual_route_id
        FROM actual_route ar
        JOIN actual_route_spot ars ON ars.actual_route_id = ar.actual_route_id
        JOIN tourist_spot ts ON ts.spot_id = ars.spot_id
        WHERE ar.is_public = true
          AND ar.is_deleted = false
          AND (:regionId IS NULL OR ts.region_id = :regionId)
          AND (:category IS NULL OR ts.category = :category)
        """, nativeQuery = true)
    List<Long> findMatchingRouteIds(@Param("regionId") String regionId,
                                    @Param("category") String category);
}