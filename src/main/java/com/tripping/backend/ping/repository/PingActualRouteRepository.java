package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.ActualRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PingActualRouteRepository extends JpaRepository<ActualRoute, Long> {

    // 본인 소유의(진행중/완료 불문) 여행인지 검증하며 조회
    Optional<ActualRoute> findByActualRouteIdAndUserIdAndIsDeletedFalse(Long actualRouteId, Long userId);
}
