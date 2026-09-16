package com.tripping.backend.home.repository;

import com.tripping.backend.entity.ActualRouteSpot;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HomeActualRouteSpotRepository extends JpaRepository<ActualRouteSpot, Long> {

    /** "이번주 인기 여행" 카드에 표시할 방문 순서대로의 스팟 목록 (spotId, 사진, 이름은 TouristSpot에서 별도 조회). */
    List<ActualRouteSpot> findByActualRouteIdOrderByVisitOrderAsc(Long actualRouteId);

    // 👈 새로 추가: "Pinger" 레벨 계산용 - 이 유저가 지금까지 찍은 핑(방문 스팟) 총 개수.
    // ActualRouteSpot ↔ ActualRoute 사이에 연관관계 매핑이 없어서(FK만 존재), 다른 쿼리들처럼
    // 두 엔티티를 콤마로 나열해 actualRouteId = actualRouteId 조건으로 직접 조인함.
    @Query("""
            select count(ars)
            from ActualRouteSpot ars, ActualRoute ar
            where ars.actualRouteId = ar.actualRouteId
              and ar.userId = :userId
              and ar.isDeleted = false
            """)
    long countByWriterUserId(@Param("userId") Long userId);

    /**
     * "떠오르는 인기 장소": 최근 visitTime 기준으로 방문(핑) 기록이 많은 관광지 spotId 를
     * 방문 횟수 내림차순으로 조회합니다. Pageable 로 상위 N개만 잘라옵니다.
     */
    @Query("""
        SELECT ars.spotId
        FROM ActualRouteSpot ars
        WHERE ars.visitTime >= :since
        GROUP BY ars.spotId
        ORDER BY COUNT(ars.spotId) DESC
        """)
    List<Long> findTrendingSpotIds(@Param("since") LocalDateTime since, Pageable pageable);

    /** spotId 하나당 최근 방문 횟수. TrendingPlaceResponse 에 표시할 카운트를 채우기 위해 사용합니다. */
    @Query("""
        SELECT COUNT(ars)
        FROM ActualRouteSpot ars
        WHERE ars.spotId = :spotId AND ars.visitTime >= :since
        """)
    long countRecentVisitsBySpotId(@Param("spotId") Long spotId, @Param("since") LocalDateTime since);

    /**
     * "내 주변 여행": 실제 방문 GPS(ActualRouteSpot.latitude/longitude) 기준으로
     * 하버사인 공식을 이용해 사용자 위치(lat, lng)와의 거리를 계산합니다.
     * 루트 하나에 여러 방문 스팟이 있을 수 있어 MIN 거리를 그 루트의 대표 거리로 씁니다.
     * ACOS/RADIANS/SIN/COS 등은 JPQL 표준 함수가 아니라 네이티브 쿼리로 작성했습니다 (PostgreSQL 기준).
     *
     * 주의: PostgreSQL은 MySQL과 달리 HAVING 절에서 SELECT 별칭을 참조할 수 없어서
     * (표준 SQL 실행 순서상 HAVING이 SELECT보다 먼저 평가됨), 거리 계산을 서브쿼리로
     * 한 번 감싼 뒤 바깥 쿼리의 WHERE 절에서 그 결과 컬럼을 필터링하는 방식으로 작성했습니다.
     */
    @Query(
        value = """
            SELECT distances.actual_route_id AS actualRouteId,
                   distances.distance_km AS distanceKm
            FROM (
                SELECT ars.actual_route_id,
                       MIN(
                           6371 * ACOS(
                               COS(RADIANS(:lat)) * COS(RADIANS(ars.latitude)) * COS(RADIANS(ars.longitude) - RADIANS(:lng))
                               + SIN(RADIANS(:lat)) * SIN(RADIANS(ars.latitude))
                           )
                       ) AS distance_km
                FROM actual_route_spot ars
                JOIN actual_route ar ON ar.actual_route_id = ars.actual_route_id
                WHERE ar.is_public = true
                  AND ar.is_deleted = false
                  AND ars.latitude IS NOT NULL
                  AND ars.longitude IS NOT NULL
                GROUP BY ars.actual_route_id
            ) distances
            WHERE distances.distance_km <= :radiusKm
            ORDER BY distances.distance_km ASC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT ars.actual_route_id,
                       MIN(
                           6371 * ACOS(
                               COS(RADIANS(:lat)) * COS(RADIANS(ars.latitude)) * COS(RADIANS(ars.longitude) - RADIANS(:lng))
                               + SIN(RADIANS(:lat)) * SIN(RADIANS(ars.latitude))
                           )
                       ) AS distance_km
                FROM actual_route_spot ars
                JOIN actual_route ar ON ar.actual_route_id = ars.actual_route_id
                WHERE ar.is_public = true
                  AND ar.is_deleted = false
                  AND ars.latitude IS NOT NULL
                  AND ars.longitude IS NOT NULL
                GROUP BY ars.actual_route_id
            ) distances
            WHERE distances.distance_km <= :radiusKm
            """,
        nativeQuery = true
    )
    Page<NearbyRouteProjection> findNearbyRoutes(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            Pageable pageable
    );

    @Query(value = """
        SELECT COALESCE(AVG(pl.rating), 0)
        FROM ping_log pl
        JOIN actual_route_spot ars ON ars.actual_route_spot_id = pl.actual_route_spot_id
        WHERE ars.spot_id = :spotId
          AND pl.rating IS NOT NULL
          AND pl.is_deleted = false
        """, nativeQuery = true)
    Double findAverageRatingBySpotId(@Param("spotId") Long spotId);

    @Query(value = """
        SELECT COUNT(DISTINCT ars.actual_route_id)
        FROM actual_route_spot ars
        WHERE ars.spot_id = :spotId
        """, nativeQuery = true)
    Long countDistinctRoutesBySpotId(@Param("spotId") Long spotId);

    // 👈 새로 추가: "인기 장소" 카드 사진용. 이 장소 후기(ping_log) 중 사진이 등록된 것들을,
    // 그 후기가 달린 루트(여행)가 얼마나 저장(찜)됐는지 기준으로 우선순위를 매겨 하나만 뽑음
    // (저장 많이 된 루트의 후기 사진일수록 신뢰도 높은 대표 사진일 거라는 가정). 저장 수가
    // 같으면(둘 다 0 포함) 최신 후기를 우선함. 사진 등록된 후기가 하나도 없으면 null.
    @Query(value = """
        SELECT pl.photo_url
        FROM ping_log pl
        JOIN actual_route_spot ars ON ars.actual_route_spot_id = pl.actual_route_spot_id
        LEFT JOIN (
            SELECT actual_route_id, COUNT(*) AS save_count
            FROM saved_route
            GROUP BY actual_route_id
        ) sr ON sr.actual_route_id = ars.actual_route_id
        WHERE ars.spot_id = :spotId
          AND pl.photo_url IS NOT NULL
          AND pl.is_deleted = false
        ORDER BY COALESCE(sr.save_count, 0) DESC, pl.created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    String findBestReviewPhotoUrlBySpotId(@Param("spotId") Long spotId);

    // 이 장소를 포함한 공개 루트들의 actual_route_id 목록
    @Query(value = """
        SELECT DISTINCT ar.actual_route_id
        FROM actual_route_spot ars
        JOIN actual_route ar ON ar.actual_route_id = ars.actual_route_id
        WHERE ars.spot_id = :spotId
          AND ar.is_public = true
          AND ar.is_deleted = false
        """, nativeQuery = true)
    List<Long> findPublicRouteIdsBySpotId(@Param("spotId") Long spotId);

    // 이 장소에 달린 후기(별점+텍스트+사진+작성일) 목록. 작성자 닉네임까지 조인해서 가져옴
    @Query(value = """
        SELECT pl.rating AS rating,
               pl.review_comment AS reviewComment,
               u.nickname AS writerNickname,
               pl.photo_url AS photoUrl,
               pl.created_at AS createdAt
        FROM ping_log pl
        JOIN actual_route_spot ars ON ars.actual_route_spot_id = pl.actual_route_spot_id
        JOIN actual_route ar ON ar.actual_route_id = ars.actual_route_id
        JOIN app_user u ON u.user_id = ar.user_id
        WHERE ars.spot_id = :spotId
          AND pl.is_deleted = false
          AND pl.review_comment IS NOT NULL
        ORDER BY pl.created_at DESC
        """, nativeQuery = true)
    List<SpotReviewProjection> findReviewsBySpotId(@Param("spotId") Long spotId);

    interface SpotReviewProjection {
        Integer getRating();
        String getReviewComment();
        String getWriterNickname();
        String getPhotoUrl();
        LocalDateTime getCreatedAt();
    }

}
