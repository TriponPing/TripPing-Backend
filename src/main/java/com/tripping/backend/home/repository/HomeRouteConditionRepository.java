package com.tripping.backend.home.repository;

import com.tripping.backend.entity.RouteCondition;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HomeRouteConditionRepository extends JpaRepository<RouteCondition, Long> {

    /** theme 값이 있는 것들만 등장 빈도 내림차순으로 집계합니다. Pageable 로 상위 N개만 잘라옵니다. */
    @Query("""
        SELECT rc.theme AS theme, COUNT(rc) AS count
        FROM RouteCondition rc
        WHERE rc.theme IS NOT NULL
        GROUP BY rc.theme
        ORDER BY COUNT(rc) DESC
        """)
    List<ThemeCountProjection> findPopularThemes(Pageable pageable);
}
