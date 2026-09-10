package com.tripping.backend.community.repository;

import com.tripping.backend.entity.SavedRoute;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 마이페이지 쪽에 이미 MyPageSavedRouteRepository(저장/저장취소용)가 있지만,
 * 여기서는 커뮤니티 루트 카드의 "저장(북마크) 수" 표시용 카운트만 필요해서 별도로 둡니다.
 */
public interface CommunitySavedRouteRepository extends JpaRepository<SavedRoute, Long> {
    long countByActualRouteId(Long actualRouteId);
}
