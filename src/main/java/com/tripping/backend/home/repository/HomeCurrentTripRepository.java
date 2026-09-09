package com.tripping.backend.home.repository;

import com.tripping.backend.entity.ActualRoute;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 홈 화면 "여행 중" 카드용. trip 도메인의 TripActualRouteRepository와 별개 인터페이스로 두어
 * (같은 ActualRoute 엔티티를 보는 리포지토리가 여러 도메인에 있어도 Spring Data JPA에서는 문제 없음)
 * trip/ping 도메인 파일을 건드리지 않고 홈 화면 전용 조회를 추가함.
 */
public interface HomeCurrentTripRepository extends JpaRepository<ActualRoute, Long> {

    // 정상적으로는 유저당 IN_PROGRESS 여행이 1개여야 하지만, 테스트/버그로 2개 이상 쌓일 수 있어서
    // Optional 단건 조회(NonUniqueResultException 위험) 대신 Pageable로 최대 1개만 가져옴.
    @Query("SELECT r FROM ActualRoute r WHERE r.userId = :userId AND r.status = 'IN_PROGRESS' AND r.isDeleted = false ORDER BY r.actualRouteId DESC")
    List<ActualRoute> findInProgressRoutes(@Param("userId") Long userId, Pageable pageable);
}
