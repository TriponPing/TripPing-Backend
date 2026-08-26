package com.tripping.backend.community.repository;

import com.tripping.backend.entity.TouristSpot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    @Query("SELECT ts.spotId FROM TouristSpot ts WHERE ts.regionId = :regionId")
    List<Long> findSpotIdsByRegionId(@Param("regionId") String regionId);
}
