package com.tripping.backend.route.repository;

import com.tripping.backend.entity.PlannedRouteSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlannedRouteSpotRepository extends JpaRepository<PlannedRouteSpot, Long> {
    List<PlannedRouteSpot> findByPlannedRouteId(Long plannedRouteId);

    // 여러 계획의 스팟을 한 번에 (N+1 방지용, 나의 여행 지도 > 내 계획에서 사용)
    List<PlannedRouteSpot> findByPlannedRouteIdInOrderByPlannedRouteIdAscVisitOrderAsc(List<Long> plannedRouteIds);
}