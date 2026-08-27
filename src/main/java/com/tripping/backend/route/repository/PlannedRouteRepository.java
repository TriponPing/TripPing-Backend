package com.tripping.backend.route.repository;

import com.tripping.backend.entity.PlannedRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlannedRouteRepository extends JpaRepository<PlannedRoute, Long> {
}