package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.ActualRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * mypage 패키지에도 같은 이름의 리포지토리가 있습니다 - 도메인별로 독립적으로 두라는
 * PACKAGE_STRUCTURE.md 규칙을 따라 ping 도메인 전용으로 따로 둔 것이며, 같은 엔티티를
 * 가리키는 별개의 Spring Data 리포지토리라 빈 충돌은 없습니다.
 */
public interface ActualRouteRepository extends JpaRepository<ActualRoute, Long> {

    // 본인 소유의(진행중/완료 불문) 여행인지 검증하며 조회
    Optional<ActualRoute> findByActualRouteIdAndUserIdAndIsDeletedFalse(Long actualRouteId, Long userId);
}
