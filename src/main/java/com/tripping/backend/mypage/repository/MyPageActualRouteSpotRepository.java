package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.ActualRouteSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyPageActualRouteSpotRepository extends JpaRepository<ActualRouteSpot, Long> {

    // 여행 하나의 스팟 전체 (방문 순서대로)
    List<ActualRouteSpot> findByActualRouteIdOrderByVisitOrderAsc(Long actualRouteId);

    // 여러 여행의 스팟을 한 번에 (N+1 방지용, 여행별 대표/전체 스팟 뽑을 때 사용)
    List<ActualRouteSpot> findByActualRouteIdInOrderByActualRouteIdAscVisitOrderAsc(List<Long> actualRouteIds);

    /**
     * 주어진 여행(routeIds)들 중에서, 포함된 관광지 이름(TOURIST_SPOT.name)에 keyword가 들어간 여행 id만 추려줌.
     * ActualRouteSpot ↔ TouristSpot 사이에 연관관계 매핑이 없고 FK(spot_id)만 있어서,
     * JPQL에서 두 엔티티를 콤마로 나열해 spotId = spotId 조건으로 직접 조인합니다.
     */
    @Query("""
            select distinct ars.actualRouteId
            from ActualRouteSpot ars, TouristSpot ts
            where ars.spotId = ts.spotId
              and ars.actualRouteId in :routeIds
              and ts.name like concat('%', :keyword, '%')
            """)
    List<Long> findRouteIdsBySpotNameKeyword(@Param("routeIds") List<Long> routeIds, @Param("keyword") String keyword);
}
