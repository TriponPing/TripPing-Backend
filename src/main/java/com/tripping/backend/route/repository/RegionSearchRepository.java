package com.tripping.backend.route.repository;

import com.tripping.backend.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegionSearchRepository extends JpaRepository<Region, String> {
    List<Region> findByRegionNameContaining(String keyword);
}