package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 일정에 담긴 spot_id를 이름·주소로 바꾸고, 관광지 검색과 TourAPI 등록에 쓴다.
// 이 프로젝트는 기능별로 TouristSpot 레포지토리를 따로 두는 방식이라 그 관례를 따른다.
public interface B2bTouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    List<TouristSpot> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);

    // 같은 관광지를 두 번 등록하지 않기 위한 확인. 관광공사 contentId가 기준이다.
    Optional<TouristSpot> findByApiContentId(String apiContentId);
}
