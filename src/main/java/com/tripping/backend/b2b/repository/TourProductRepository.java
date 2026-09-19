package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.TourProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 상품은 기관 단위로 격리된다. orgId를 조건에 넣지 않으면 다른 기관 상품이
// 조회되므로, 단건 조회도 productId만으로 찾지 않는다.
public interface TourProductRepository extends JpaRepository<TourProduct, Long> {

    List<TourProduct> findByOrgIdAndIsDeletedFalseOrderByUpdatedAtDesc(Long orgId);

    Optional<TourProduct> findByProductIdAndOrgIdAndIsDeletedFalse(Long productId, Long orgId);
}
