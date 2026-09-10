package com.tripping.backend.global.config;

import com.tripping.backend.entity.Region;
import com.tripping.backend.entity.RegionType;
import com.tripping.backend.route.repository.RegionSearchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * region 테이블에 대한민국 17개 시/도 데이터를 채워둠 (회원가입 지역 선택 등에서 사용).
 * 서울/부산/강원/제주(R01~R04)는 이미 있었고, 나머지 13개(R05~R17)를 여기서 보충함.
 * 이미 있는 regionId는 건너뛰므로 서버를 여러 번 재시작해도 중복 저장되지 않음.
 */
@Component
@RequiredArgsConstructor
public class RegionSeeder implements CommandLineRunner {

    private final RegionSearchRepository regionRepository;

    @Override
    public void run(String... args) {
        List<Region> regions = List.of(
                Region.builder().regionId("R05").regionName("경기").regionType(RegionType.도).build(),
                Region.builder().regionId("R06").regionName("인천").regionType(RegionType.광역시).build(),
                Region.builder().regionId("R07").regionName("대구").regionType(RegionType.광역시).build(),
                Region.builder().regionId("R08").regionName("광주").regionType(RegionType.광역시).build(),
                Region.builder().regionId("R09").regionName("대전").regionType(RegionType.광역시).build(),
                Region.builder().regionId("R10").regionName("울산").regionType(RegionType.광역시).build(),
                Region.builder().regionId("R11").regionName("세종").regionType(RegionType.특별자치시).build(),
                Region.builder().regionId("R12").regionName("충북").regionType(RegionType.도).build(),
                Region.builder().regionId("R13").regionName("충남").regionType(RegionType.도).build(),
                Region.builder().regionId("R14").regionName("전북").regionType(RegionType.특별자치도).build(),
                Region.builder().regionId("R15").regionName("전남").regionType(RegionType.도).build(),
                Region.builder().regionId("R16").regionName("경북").regionType(RegionType.도).build(),
                Region.builder().regionId("R17").regionName("경남").regionType(RegionType.도).build()
        );

        for (Region region : regions) {
            if (!regionRepository.existsById(region.getRegionId())) {
                regionRepository.save(region);
            }
        }
    }
}
