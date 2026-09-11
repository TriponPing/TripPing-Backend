package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyPagePingLogRepository extends JpaRepository<PingLog, Long> {

    // 여행 기록 상세 조회 시, 스팟별 평점/사진/리뷰를 한 번에 가져오기 위함
    List<PingLog> findByActualRouteSpotIdInAndIsDeletedFalse(List<Long> actualRouteSpotIds);

    // 뱃지 "이야기꾼" 달성 여부 판단용 - 이 유저가 후기(리뷰 코멘트)를 남긴 핑 개수.
    // PingLog ↔ ActualRouteSpot ↔ ActualRoute 둘 다 연관관계 매핑이 없어서 콤마로 직접 조인.
    @Query("""
            select count(pl)
            from PingLog pl, ActualRouteSpot ars, ActualRoute ar
            where pl.actualRouteSpotId = ars.actualRouteSpotId
              and ars.actualRouteId = ar.actualRouteId
              and ar.userId = :userId
              and ar.isDeleted = false
              and pl.isDeleted = false
              and pl.reviewComment is not null
            """)
    long countReviewsByUserId(@Param("userId") Long userId);

    // 뱃지 "별빛 감별사" 달성 여부 판단용 - 이 유저가 별점을 남긴 핑 개수
    @Query("""
            select count(pl)
            from PingLog pl, ActualRouteSpot ars, ActualRoute ar
            where pl.actualRouteSpotId = ars.actualRouteSpotId
              and ars.actualRouteId = ar.actualRouteId
              and ar.userId = :userId
              and ar.isDeleted = false
              and pl.isDeleted = false
              and pl.rating is not null
            """)
    long countRatedPingsByUserId(@Param("userId") Long userId);
}
