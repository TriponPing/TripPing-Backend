package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.ActualRouteSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PingActualRouteSpotRepository extends JpaRepository<ActualRouteSpot, Long> {

    // 여행에 다음 방문 순서(visit_order)를 계산하기 위해 필요
    List<ActualRouteSpot> findByActualRouteIdOrderByVisitOrderAsc(Long actualRouteId);
}