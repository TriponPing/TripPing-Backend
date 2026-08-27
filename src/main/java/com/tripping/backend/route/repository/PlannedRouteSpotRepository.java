package com.tripping.backend.route.repository;

import com.tripping.backend.entity.PlannedRouteSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlannedRouteSpotRepository extends JpaRepository<PlannedRouteSpot, Long> {
    List<PlannedRouteSpot> findByPlannedRouteId(Long plannedRouteId);
}