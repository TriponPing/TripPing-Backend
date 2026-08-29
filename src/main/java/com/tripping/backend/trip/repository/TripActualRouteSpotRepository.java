package com.tripping.backend.trip.repository;

import com.tripping.backend.entity.ActualRouteSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripActualRouteSpotRepository extends JpaRepository<ActualRouteSpot, Long> {
    List<ActualRouteSpot> findByActualRouteId(Long actualRouteId);
}