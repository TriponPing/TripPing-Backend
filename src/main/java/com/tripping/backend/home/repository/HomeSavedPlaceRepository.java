package com.tripping.backend.home.repository;

import com.tripping.backend.entity.SavedPlace;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * mypage/place 도메인에 이미 있는 SavedPlace(장소 저장/북마크)를 그대로 재사용해서
 * "이번주 인기 장소" 집계용 쿼리만 home 도메인 전용으로 따로 둡니다.
 * (HomeSavedRouteRepository가 SavedRoute를 재사용하는 것과 같은 컨벤션)
 */
public interface HomeSavedPlaceRepository extends JpaRepository<SavedPlace, Long> {

    /**
     * 기준 시각(since) 이후 저장된 횟수가 많은 순으로 spotId 목록을 조회합니다.
     * Pageable로 상위 N개만 잘라옵니다.
     */
    @Query("""
        SELECT sp.spotId
        FROM SavedPlace sp
        WHERE sp.createdAt >= :since
        GROUP BY sp.spotId
        ORDER BY COUNT(sp.spotId) DESC
        """)
    List<Long> findPopularSpotIds(@Param("since") LocalDateTime since, Pageable pageable);

    /** 특정 장소의 최근 저장 횟수. PopularPlaceResponse에 표시할 savedCount를 채우기 위해 사용합니다. */
    @Query("""
        SELECT COUNT(sp)
        FROM SavedPlace sp
        WHERE sp.spotId = :spotId AND sp.createdAt >= :since
        """)
    long countRecentSavesBySpotId(@Param("spotId") Long spotId, @Param("since") LocalDateTime since);
}
