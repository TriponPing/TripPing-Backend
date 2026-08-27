package com.tripping.backend.route.service;

import com.tripping.backend.route.dto.RegionSearchResponse;
import com.tripping.backend.route.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    public List<RegionSearchResponse> searchRegions(String keyword) {
        return regionRepository.findByRegionNameContaining(keyword).stream()
                .map(RegionSearchResponse::new)
                .toList();
    }
}