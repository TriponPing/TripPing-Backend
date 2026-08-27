package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PingLogRepository extends JpaRepository<PingLog, Long> {

    Optional<PingLog> findByActualRouteSpotIdAndIsDeletedFalse(Long actualRouteSpotId);
}
