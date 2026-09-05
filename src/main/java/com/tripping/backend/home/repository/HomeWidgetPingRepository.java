package com.tripping.backend.home.repository;

import com.tripping.backend.entity.WidgetPing;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 홈 화면 "여행 중" 카드에서 실제 Ping 개수/방문 장소 이름을 보여주기 위한 조회 전용 리포지토리.
 * Ping 도메인 코드는 건드리지 않고, WidgetPing 엔티티만 홈 도메인에서 읽기 전용으로 조회함.
 */
public interface HomeWidgetPingRepository extends JpaRepository<WidgetPing, Long> {

    long countByActualRouteIdAndIsDeletedFalse(Long actualRouteId);

    @Query("""
        SELECT wp.placeName
        FROM WidgetPing wp
        WHERE wp.actualRouteId = :actualRouteId AND wp.isDeleted = false
        ORDER BY wp.pingTime ASC
        """)
    List<String> findVisitedPlaceNames(@Param("actualRouteId") Long actualRouteId);
}
