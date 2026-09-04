package com.tripping.backend.home.repository;

import com.tripping.backend.entity.ActualRoute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ping/mypage/trip/community 등 다른 도메인도 각자 ActualRoute 용 Repository를 따로 갖고 있는
 * 것과 같은 컨벤션으로, home 도메인 전용 Repository를 별도로 둡니다. (Home 접두사)
 */
public interface HomeActualRouteRepository extends JpaRepository<ActualRoute, Long> {

    List<ActualRoute> findByActualRouteIdInAndIsPublicTrueAndIsDeletedFalse(List<Long> actualRouteIds);
}
