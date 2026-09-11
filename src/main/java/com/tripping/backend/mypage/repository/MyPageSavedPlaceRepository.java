package com.tripping.backend.mypage.repository;

import com.tripping.backend.entity.SavedPlace;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyPageSavedPlaceRepository extends JpaRepository<SavedPlace, Long> {

    // 저장한 장소 목록 조회 - GET /users/me/places/saved (최근 저장한 순)
    Page<SavedPlace> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 나의 여행 지도 > 저장한 장소 탭용 - 페이징 없이 전체
    List<SavedPlace> findByUserId(Long userId);

    // 카드에 표시할 "저장 N" - 이 장소를 저장한 전체 유저 수(전체 기간, 본인 포함)
    long countBySpotId(Long spotId);

    // 뱃지 "보물 창고" 달성 여부 판단용 - 이 유저가 저장한 장소 개수
    long countByUserId(Long userId);
}
