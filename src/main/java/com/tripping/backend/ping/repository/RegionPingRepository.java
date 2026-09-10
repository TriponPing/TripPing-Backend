package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.RegionPing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionPingRepository extends JpaRepository<RegionPing, Long> {
}
