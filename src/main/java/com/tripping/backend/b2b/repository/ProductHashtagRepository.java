package com.tripping.backend.b2b.repository;

import com.tripping.backend.entity.ProductHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductHashtagRepository extends JpaRepository<ProductHashtag, Long> {

    List<ProductHashtag> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
