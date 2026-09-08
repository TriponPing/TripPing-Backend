package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PingTouristSpotRepository extends JpaRepository<TouristSpot, Long> {
}