package com.tripping.backend.place.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PlaceTouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    // 카테고리별 조회 (관광지/맛집/카페 공통)
    List<TouristSpot> findByCategory(String category);

    // 카테고리 + 지역 필터 조회
    List<TouristSpot> findByCategoryAndRegionId(String category, String regionId);

    // 지역별 전체 장소 조회 (추천 알고리즘용 후보군)
    List<TouristSpot> findByRegionId(String regionId);

    List<TouristSpot> findByRegionIdAndNameContainingIgnoreCase(String regionId, String name);
    List<TouristSpot> findByNameContainingIgnoreCase(String name);

    List<TouristSpot> findByDescriptionIsNull(org.springframework.data.domain.Pageable pageable);

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

    // "동네핑거가 등록한" 장소: 등록자가 그 지역 거주 + 주민핑거 인증된 경우만
    @Query(value = """
        SELECT ts.* FROM tourist_spot ts
        JOIN app_user u ON u.user_id = ts.created_by_user_id
        WHERE ts.category = :category
          AND u.region_id = :regionId
          AND u.is_resident_pinger = true
        """, nativeQuery = true)
    List<TouristSpot> findByCategoryAndCreatorRegionAndResidentPinger(
            @Param("category") String category,
            @Param("regionId") String regionId
    );
}