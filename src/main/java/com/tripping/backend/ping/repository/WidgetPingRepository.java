package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.WidgetPing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WidgetPingRepository extends JpaRepository<WidgetPing, Long> {

    // 여행 하나의 핑 전체 (등록 순서대로) - 진행 중 조회 / 기록 조회 공용
    List<WidgetPing> findByActualRouteIdAndIsDeletedFalseOrderByPingTimeAsc(Long actualRouteId);
    // 2. 💡 [추가] 특정 장소(spotId)의 시간대 구간별 핑 개수 집계 쿼리
    @Query("SELECT " +
            "CASE " +
            "  WHEN HOUR(w.pingTime) BETWEEN 6 AND 8 THEN '아침' " +
            "  WHEN HOUR(w.pingTime) BETWEEN 9 AND 11 THEN '오전' " +
            "  WHEN HOUR(w.pingTime) BETWEEN 12 AND 17 THEN '오후' " +
            "  WHEN HOUR(w.pingTime) BETWEEN 18 AND 21 THEN '저녁' " +
            "  ELSE '밤/새벽' " +
            "END as timeSlot, " +
            "COUNT(w) as cnt " +
            "FROM WidgetPing w " +
            "WHERE w.spotId = :spotId AND w.isDeleted = false " +
            "GROUP BY timeSlot " +
            "ORDER BY cnt DESC")
    List<Object[]> countPingsByTimeSlot(@Param("spotId") Long spotId);

    // 3. 💡 [추가] 특정 장소의 총 핑 개수 조회
    long countBySpotIdAndIsDeletedFalse(Long spotId);
}

