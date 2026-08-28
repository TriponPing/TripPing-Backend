package com.tripping.backend.route.service;

import com.tripping.backend.route.dto.RegionSearchResponse;
import com.tripping.backend.route.repository.RegionSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionSearchRepository regionRepository;

    public List<RegionSearchResponse> searchRegions(String keyword) {
        return regionRepository.findByRegionNameContaining(keyword).stream()
                .map(RegionSearchResponse::new)
                .toList();
    }
}