package com.tripping.backend.route.repository;

import com.tripping.backend.entity.PlannedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PlannedRouteRepository extends JpaRepository<PlannedRoute, Long> {
    List<PlannedRoute> findByTitleContaining(String keyword);

    // 나의 여행 지도 > 내 계획 - 아직 실제 여행으로 시작 안 한 계획만
    // isStarted가 NULL인 기존 데이터도 "안 시작함"으로 취급해야 해서 derived query 대신 직접 씀
    @Query("SELECT p FROM PlannedRoute p WHERE p.userId = :userId AND p.isDeleted = false " +
            "AND (p.isStarted = false OR p.isStarted IS NULL)")
    List<PlannedRoute> findByUserIdAndIsDeletedFalseAndIsStartedFalse(@Param("userId") Long userId);
}