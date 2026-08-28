package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.ActualRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyPageActualRouteRepository extends JpaRepository<ActualRoute, Long> {

    // 다녀온 여행 목록 조회(요약) - 최근 5개
    List<ActualRoute> findTop5ByUserIdAndIsDeletedFalseOrderByTravelDateDesc(Long userId);

    // 다녀온 여행 전체 목록 조회 (페이징)
    Page<ActualRoute> findByUserIdAndIsDeletedFalseOrderByTravelDateDesc(Long userId, Pageable pageable);

    // 여행 기록 상세 조회 - 본인 소유인지까지 함께 검증
    Optional<ActualRoute> findByActualRouteIdAndUserIdAndIsDeletedFalse(Long actualRouteId, Long userId);

    // 나의 여행 지도(drawn) - 내가 다녀온 여행 전체
    List<ActualRoute> findByUserIdAndIsDeletedFalse(Long userId);
}
