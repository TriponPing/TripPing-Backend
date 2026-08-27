package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.WidgetPing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WidgetPingRepository extends JpaRepository<WidgetPing, Long> {

    // 여행 하나의 핑 전체 (등록 순서대로) - 진행 중 조회 / 기록 조회 공용
    List<WidgetPing> findByActualRouteIdAndIsDeletedFalseOrderByPingTimeAsc(Long actualRouteId);
}
