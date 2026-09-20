package com.tripping.backend.insight.repository;

import com.tripping.backend.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// "상품 기획안" 보고서의 실제 방문자 반응 근거(평점·후기)용 조회.
// B2bAlternativeSpotRepository와 같은 ping_log 조인 패턴을 쓰되, 여긴 거리 계산 없이
// 주어진 spotId 목록 전체에 대한 평점/후기만 모은다. TouristSpot 레포지토리를 새로
// 두는 이 프로젝트의 관례(기능별로 분리)를 그대로 따른다.
public interface SpotEvidenceRepository extends JpaRepository<TouristSpot, Long> {

    @Query(value = """
        SELECT AVG(pl.rating) AS average_rating, COUNT(pl.rating) AS rating_count
        FROM ping_log pl
        JOIN actual_route_spot ars ON ars.actual_route_spot_id = pl.actual_route_spot_id
        WHERE ars.spot_id IN (:spotIds)
          AND pl.is_deleted = false
          AND pl.rating IS NOT NULL
        """, nativeQuery = true)
    RatingRow findAverageRating(@Param("spotIds") List<Long> spotIds);

    @Query(value = """
        SELECT pl.review_comment
        FROM ping_log pl
        JOIN actual_route_spot ars ON ars.actual_route_spot_id = pl.actual_route_spot_id
        WHERE ars.spot_id IN (:spotIds)
          AND pl.is_deleted = false
          AND pl.review_comment IS NOT NULL
          AND pl.review_comment <> ''
        ORDER BY pl.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<String> findRecentComments(@Param("spotIds") List<Long> spotIds, @Param("limit") int limit);

    interface RatingRow {
        Double getAverageRating();
        Long getRatingCount();
    }
}
