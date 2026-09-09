package com.tripping.backend.trip.repository;

import com.tripping.backend.entity.ActualRoute;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripActualRouteRepository extends JpaRepository<ActualRoute, Long> {

    // 유저의 진행 중(IN_PROGRESS)인 여행 조회. 정상적으로는 1개여야 하지만 혹시 2개 이상
    // 남아있어도 Pageable로 원하는 개수만 안전하게 가져올 수 있음(Optional 단건 조회 X).
    @Query("SELECT r FROM ActualRoute r WHERE r.userId = :userId AND r.status = 'IN_PROGRESS' AND r.isDeleted = false ORDER BY r.actualRouteId DESC")
    List<ActualRoute> findInProgressRoutes(@Param("userId") Long userId, Pageable pageable);

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
