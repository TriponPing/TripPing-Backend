package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

// 일정에 담긴 spot_id를 이름·주소로 바꾸기 위해서만 쓴다. 이 프로젝트는
// 기능별로 TouristSpot 레포지토리를 따로 두는 방식이라 그 관례를 따른다.
public interface B2bTouristSpotRepository extends JpaRepository<TouristSpot, Long> {
}
