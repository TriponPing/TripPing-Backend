package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.RegionPing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyPageRegionPingRepository extends JpaRepository<RegionPing, Long> {

    // 뱃지 "동네 탐험대장" 달성 여부 판단용 - 이 유저가 지금까지 등록한 지역핑 개수
    long countByUserId(Long userId);
}
