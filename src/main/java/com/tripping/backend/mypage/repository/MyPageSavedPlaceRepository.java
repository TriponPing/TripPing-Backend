package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.SavedPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyPageSavedPlaceRepository extends JpaRepository<SavedPlace, Long> {

    // 저장한 장소 목록 조회 - GET /users/me/places/saved (최근 저장한 순)
    Page<SavedPlace> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 카드에 표시할 "저장 N" - 이 장소를 저장한 전체 유저 수(전체 기간, 본인 포함)
    long countBySpotId(Long spotId);
}
