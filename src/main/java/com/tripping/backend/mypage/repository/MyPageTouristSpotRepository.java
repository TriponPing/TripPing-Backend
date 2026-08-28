package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyPageTouristSpotRepository extends JpaRepository<TouristSpot, Long> {
}
