package com.tripping.backend.place.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    // 카테고리별 조회 (관광지/맛집/카페 공통)
    List<TouristSpot> findByCategory(String category);

    // 카테고리 + 지역 필터 조회
    List<TouristSpot> findByCategoryAndRegionId(String category, String regionId);

    // 지도 기반 반경 검색 (Haversine 공식)
    @Query(value = """
        SELECT * FROM tourist_spot t
        WHERE (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(t.latitude)) *
                cos(radians(t.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(t.latitude))
            )
        ) <= :radius
        """, nativeQuery = true)
    List<TouristSpot> findWithinRadius(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radiusKm
    );
}