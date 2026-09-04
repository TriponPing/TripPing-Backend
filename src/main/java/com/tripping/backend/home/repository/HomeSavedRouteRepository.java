package com.tripping.backend.home.repository;

import com.tripping.backend.entity.SavedRoute;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 마이페이지 쪽에 이미 MyPageSavedRouteRepository(저장/저장취소용)가 있지만,
 * 여기서는 "이번주 인기 여행" 집계용 쿼리가 필요해서 home 도메인 전용으로 따로 둡니다.
 */
public interface HomeSavedRouteRepository extends JpaRepository<SavedRoute, Long> {

    /**
     * 기준 시각(since) 이후 저장된 횟수가 많은 순으로 actualRouteId 목록을 조회합니다.
     * Pageable 로 상위 N개만 잘라옵니다.
     */
    @Query("""
        SELECT sr.actualRouteId
        FROM SavedRoute sr
        WHERE sr.createdAt >= :since
        GROUP BY sr.actualRouteId
        ORDER BY COUNT(sr.actualRouteId) DESC
        """)
    List<Long> findPopularRouteIds(@Param("since") LocalDateTime since, Pageable pageable);

    /** 특정 루트의 최근 저장 횟수. PopularTripResponse 에 표시할 savedCount 를 채우기 위해 사용합니다. */
    @Query("""
        SELECT COUNT(sr)
        FROM SavedRoute sr
        WHERE sr.actualRouteId = :routeId AND sr.createdAt >= :since
        """)
    long countRecentSavesByRouteId(@Param("routeId") Long routeId, @Param("since") LocalDateTime since);
}
