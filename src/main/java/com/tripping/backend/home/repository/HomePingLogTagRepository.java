package com.tripping.backend.home.repository;

import com.tripping.backend.entity.PingLogTag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ping 도메인에 이미 PingLogTagRepository(후기 등록/수정용)가 있지만,
 * 여기서는 "이번주 인기 키워드" 집계용 쿼리가 필요해서 home 도메인 전용으로 따로 둡니다.
 * (HomeSavedRouteRepository와 동일한 이유/패턴)
 */
public interface HomePingLogTagRepository extends JpaRepository<PingLogTag, Long> {

    /**
     * 기준 시각(since) 이후 작성된 후기에 달린 해시태그를 등장 빈도 내림차순으로 집계합니다.
     * 삭제된 후기(isDeleted=true)는 제외합니다. Pageable로 상위 N개만 잘라옵니다.
     */
    @Query("""
        SELECT t.name AS keyword, COUNT(plt) AS count
        FROM PingLogTag plt
        JOIN plt.tag t
        JOIN plt.pingLog pl
        WHERE pl.createdAt >= :since AND pl.isDeleted = false
        GROUP BY t.name
        ORDER BY COUNT(plt) DESC
        """)
    List<TagCountProjection> findPopularTags(@Param("since") LocalDateTime since, Pageable pageable);
}
