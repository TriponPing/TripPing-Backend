package com.tripping.backend.place.repository;

import com.tripping.backend.entity.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;



public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    Optional<SavedPlace> findByUserIdAndSpotId(Long userId, Long spotId);
    void deleteByUserIdAndSpotId(Long userId, Long spotId);
    boolean existsByUserIdAndSpotId(Long userId, Long spotId);
    List<SavedPlace> findByUserId(Long userId);
}