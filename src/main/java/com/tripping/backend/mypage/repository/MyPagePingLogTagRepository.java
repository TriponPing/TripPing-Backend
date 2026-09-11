package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.PingLogTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MyPagePingLogTagRepository extends JpaRepository<PingLogTag, Long> {

    // 뱃지 "태그 수집가" 달성 여부 판단용 - 이 유저가 남긴 핑에 달린 태그 총 개수.
    // PingLogTag -> PingLog는 실제 연관관계 매핑이 있어서(plt.pingLog) 그대로 타고 가되,
    // PingLog ↔ ActualRouteSpot ↔ ActualRoute는 매핑이 없어 콤마로 직접 조인.
    @Query("""
            select count(plt)
            from PingLogTag plt, ActualRouteSpot ars, ActualRoute ar
            where plt.pingLog.actualRouteSpotId = ars.actualRouteSpotId
              and ars.actualRouteId = ar.actualRouteId
              and ar.userId = :userId
              and ar.isDeleted = false
              and plt.pingLog.isDeleted = false
            """)
    long countByUserId(@Param("userId") Long userId);
}
