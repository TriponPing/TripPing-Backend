package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.TourProductSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourProductSpotRepository extends JpaRepository<TourProductSpot, Long> {

    List<TourProductSpot> findByProductIdOrderByVisitOrderAsc(Long productId);

    void deleteByProductId(Long productId);
}
