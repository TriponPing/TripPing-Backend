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

    // 회원가입 지역 선택 등 전체 목록이 필요한 화면용
    public List<RegionSearchResponse> getAllRegions() {
        return regionRepository.findAll().stream()
                .map(RegionSearchResponse::new)
                .toList();
    }

    public List<RegionSearchResponse> searchRegions(String keyword) {
        return regionRepository.findByRegionNameContaining(keyword).stream()
                .map(RegionSearchResponse::new)
                .toList();
    }
}
