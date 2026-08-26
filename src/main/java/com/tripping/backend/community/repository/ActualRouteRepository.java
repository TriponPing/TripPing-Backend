package com.tripping.backend.community.repository;

import com.tripping.backend.entity.ActualRoute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActualRouteRepository extends JpaRepository<ActualRoute, Long> {

    Optional<ActualRoute> findByActualRouteIdAndIsDeletedFalse(Long actualRouteId);

    Page<ActualRoute> findByActualRouteIdInAndIsPublicTrueAndIsDeletedFalseOrderByCreatedAtDesc(
            List<Long> actualRouteIds, Pageable pageable);
}
