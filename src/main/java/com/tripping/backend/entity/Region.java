package com.tripping.backend.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Region {

    @Id
    @Column(name = "region_id", length = 3)
    private String regionId; // R0~R19 코드

    @Column(name = "region_name", length = 30, nullable = false)
    private String regionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_type", nullable = false)
    private RegionType regionType;

    // 👈 새로 추가: 한국관광공사 OpenAPI(TourAPI/DataLab 등)가 요구하는 시도 단위 지역코드.
    // 우리 자체 regionId("R01" 등)와는 완전히 다른 체계라 별도 컬럼으로 매핑해둠.
    // 값은 GlobalConfig의 RegionSeeder에서 채워짐 (예: 제주=39, 서울=1).
    @Column(name = "api_area_cd", length = 10)
    private String apiAreaCd;
}
