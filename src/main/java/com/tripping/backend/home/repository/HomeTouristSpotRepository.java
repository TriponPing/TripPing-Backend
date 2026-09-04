package com.tripping.backend.home.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeTouristSpotRepository extends JpaRepository<TouristSpot, Long> {
    // findAllById(Iterable<Long>) 는 JpaRepository 기본 제공 메서드로 그대로 사용합니다.
}
