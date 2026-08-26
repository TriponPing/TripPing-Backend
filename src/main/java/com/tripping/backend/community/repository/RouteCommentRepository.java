package com.tripping.backend.community.repository;

import com.tripping.backend.entity.RouteComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteCommentRepository extends JpaRepository<RouteComment, Long> {

    Page<RouteComment> findByActualRouteIdAndIsDeletedFalse(Long actualRouteId, Pageable pageable);
}
