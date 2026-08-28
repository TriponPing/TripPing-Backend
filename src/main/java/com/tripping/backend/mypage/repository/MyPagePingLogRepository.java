package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyPagePingLogRepository extends JpaRepository<PingLog, Long> {

    // 여행 기록 상세 조회 시, 스팟별 평점/사진/리뷰를 한 번에 가져오기 위함
    List<PingLog> findByActualRouteSpotIdInAndIsDeletedFalse(List<Long> actualRouteSpotIds);
}
