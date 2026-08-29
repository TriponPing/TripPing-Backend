package com.tripping.backend.trip.repository;

import com.tripping.backend.entity.ActualRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripActualRouteRepository extends JpaRepository<ActualRoute, Long> {
}