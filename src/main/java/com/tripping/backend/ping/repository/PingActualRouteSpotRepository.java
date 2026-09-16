package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.ActualRouteSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PingActualRouteSpotRepository extends JpaRepository<ActualRouteSpot, Long> {

    // 여행에 다음 방문 순서(visit_order)를 계산하기 위해 필요
    List<ActualRouteSpot> findByActualRouteIdOrderByVisitOrderAsc(Long actualRouteId);

    // 👈 새로 추가: "다음 핑 찍기" 큐 - 아직 방문 확정(visit_time) 안 된 계획된 장소들을
    // 방문 순서대로. 맨 앞이 이번에 확정할 장소, 그 다음이 "다음 핑" 미리보기용.
    List<ActualRouteSpot> findByActualRouteIdAndVisitTimeIsNullOrderByVisitOrderAsc(Long actualRouteId);
}