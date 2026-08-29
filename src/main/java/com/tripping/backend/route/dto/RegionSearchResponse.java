package com.tripping.backend.route.dto;

import com.tripping.backend.entity.Region;
import lombok.Getter;

@Getter
public class RegionSearchResponse {
    private String regionId;
    private String regionName;
    private String regionType;

    public RegionSearchResponse(Region region) {
        this.regionId = region.getRegionId();
        this.regionName = region.getRegionName();
        this.regionType = region.getRegionType().name();
    }
}