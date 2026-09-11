package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.SavedRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyPageSavedRouteRepository extends JpaRepository<SavedRoute, Long> {

    // 저장한 루트 목록 조회 (페이징)
    Page<SavedRoute> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 나의 여행 지도용 - 페이징 없이 전체
    List<SavedRoute> findByUserId(Long userId);

    // 루트 저장 취소(북마크 해제) 시 조회
    Optional<SavedRoute> findByUserIdAndActualRouteId(Long userId, Long actualRouteId);

    // 루트 저장(북마크) 시 중복 저장 방지용
    boolean existsByUserIdAndActualRouteId(Long userId, Long actualRouteId);

    // 뱃지 "보물 창고" 달성 여부 판단용 - 이 유저가 저장한 루트 개수
    long countByUserId(Long userId);
}
