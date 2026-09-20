package com.tripping.backend.insight.repository;

import com.tripping.backend.entity.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 트렌드 화면에서 선택한 지역명(region_name)으로 관광공사 areaCd(apiAreaCd)를 찾기 위한
// 조회 전용 리포지토리. 기존 community/route 쪽 RegionRepository들과 별개로 인사이트
// 도메인 안에서만 쓴다 (엔티티는 같은 Region을 그대로 씀).
public interface InsightRegionRepository extends JpaRepository<Region, String> {
    Optional<Region> findByRegionName(String regionName);
}
