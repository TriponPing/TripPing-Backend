package com.tripping.backend.community.repository;

import com.tripping.backend.entity.ActualRouteSpot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActualRouteSpotRepository extends JpaRepository<ActualRouteSpot, Long> {

    /**
     * ActualRouteSpot 은 ActualRoute/TouristSpot 과 연관관계(ManyToOne)가 없고
     * actualRouteId / spotId 를 Long 컬럼으로만 들고 있어서, "JOIN" 대신
     * spotId 목록으로 actualRouteId 목록을 뽑아내는 방식으로 조회합니다.
     */
    @Query("SELECT DISTINCT ars.actualRouteId FROM ActualRouteSpot ars WHERE ars.spotId IN :spotIds")
    List<Long> findDistinctActualRouteIdsBySpotIdIn(@Param("spotIds") List<Long> spotIds);

    // 루트 목록 카드에 "강남 → 코엑스 → 석촌호수" 같은 경유지 미리보기를 보여주기 위한 일괄 조회
    List<ActualRouteSpot> findByActualRouteIdInOrderByActualRouteIdAscVisitOrderAsc(List<Long> actualRouteIds);
}
