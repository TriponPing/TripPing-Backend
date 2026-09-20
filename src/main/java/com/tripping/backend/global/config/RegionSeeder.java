package com.tripping.backend.global.config;

import com.tripping.backend.entity.Region;
import com.tripping.backend.entity.RegionType;
import com.tripping.backend.route.repository.RegionSearchRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * region 테이블에 대한민국 17개 시/도 데이터를 채워둠 (회원가입 지역 선택 등에서 사용).
 * 서울/부산/강원/제주(R01~R04)는 이미 있었고, 나머지 13개(R05~R17)를 여기서 보충함.
 * 이미 있는 regionId는 건너뛰므로 서버를 여러 번 재시작해도 중복 저장되지 않음.
 *
 * 👈 새로 추가: 한국관광공사 OpenAPI(TourAPI/DataLab/TatsCnctrRate 등)를 부르려면 시도 단위
 * 지역코드(areaCd)가 필요한데, 우리 regionId("R01" 등)와는 완전히 다른 체계라 apiAreaCd
 * 컬럼으로 따로 매핑해서 채워넣음. 이 표는 관광공사가 고정해둔 공식 지역코드라 바뀌지 않음.
 * 새로 생성하는 13개뿐 아니라, 이미 DB에 있던 R01~R04(서울/부산/강원/제주)도 apiAreaCd가
 * 비어있을 수 있어서 마지막에 한 번 더 훑으며 채워준다(값이 없을 때만 갱신 - 앱 재시작해도
 * 안전).
 */
@Component
@RequiredArgsConstructor
public class RegionSeeder implements CommandLineRunner {

    private final RegionSearchRepository regionRepository;

    // 관광공사 OpenAPI 공식 시도 단위 지역코드(areaCd). 17개 시/도 전체.
    private static final Map<String, String> AREA_CD_BY_REGION_ID = Map.ofEntries(
            Map.entry("R01", "1"),   // 서울
            Map.entry("R02", "6"),   // 부산
            Map.entry("R03", "32"),  // 강원
            Map.entry("R04", "39"),  // 제주
            Map.entry("R05", "31"),  // 경기
            Map.entry("R06", "2"),   // 인천
            Map.entry("R07", "4"),   // 대구
            Map.entry("R08", "5"),   // 광주
            Map.entry("R09", "3"),   // 대전
            Map.entry("R10", "7"),   // 울산
            Map.entry("R11", "8"),   // 세종
            Map.entry("R12", "33"),  // 충북
            Map.entry("R13", "34"),  // 충남
            Map.entry("R14", "37"),  // 전북
            Map.entry("R15", "38"),  // 전남
            Map.entry("R16", "35"),  // 경북
            Map.entry("R17", "36")   // 경남
    );

    @Override
    public void run(String... args) {
        List<Region> regions = List.of(
                Region.builder().regionId("R05").regionName("경기").regionType(RegionType.도).apiAreaCd(AREA_CD_BY_REGION_ID.get("R05")).build(),
                Region.builder().regionId("R06").regionName("인천").regionType(RegionType.광역시).apiAreaCd(AREA_CD_BY_REGION_ID.get("R06")).build(),
                Region.builder().regionId("R07").regionName("대구").regionType(RegionType.광역시).apiAreaCd(AREA_CD_BY_REGION_ID.get("R07")).build(),
                Region.builder().regionId("R08").regionName("광주").regionType(RegionType.광역시).apiAreaCd(AREA_CD_BY_REGION_ID.get("R08")).build(),
                Region.builder().regionId("R09").regionName("대전").regionType(RegionType.광역시).apiAreaCd(AREA_CD_BY_REGION_ID.get("R09")).build(),
                Region.builder().regionId("R10").regionName("울산").regionType(RegionType.광역시).apiAreaCd(AREA_CD_BY_REGION_ID.get("R10")).build(),
                Region.builder().regionId("R11").regionName("세종").regionType(RegionType.특별자치시).apiAreaCd(AREA_CD_BY_REGION_ID.get("R11")).build(),
                Region.builder().regionId("R12").regionName("충북").regionType(RegionType.도).apiAreaCd(AREA_CD_BY_REGION_ID.get("R12")).build(),
                Region.builder().regionId("R13").regionName("충남").regionType(RegionType.도).apiAreaCd(AREA_CD_BY_REGION_ID.get("R13")).build(),
                Region.builder().regionId("R14").regionName("전북").regionType(RegionType.특별자치도).apiAreaCd(AREA_CD_BY_REGION_ID.get("R14")).build(),
                Region.builder().regionId("R15").regionName("전남").regionType(RegionType.도).apiAreaCd(AREA_CD_BY_REGION_ID.get("R15")).build(),
                Region.builder().regionId("R16").regionName("경북").regionType(RegionType.도).apiAreaCd(AREA_CD_BY_REGION_ID.get("R16")).build(),
                Region.builder().regionId("R17").regionName("경남").regionType(RegionType.도).apiAreaCd(AREA_CD_BY_REGION_ID.get("R17")).build()
        );

        for (Region region : regions) {
            if (!regionRepository.existsById(region.getRegionId())) {
                regionRepository.save(region);
            }
        }

        // 이미 있던 R01~R04(서울/부산/강원/제주)를 포함해서, apiAreaCd가 비어있는 지역이 있으면
        // 채워준다. (신규 생성분은 이미 위에서 값이 들어가 있으므로 사실상 R01~R04만 해당)
        AREA_CD_BY_REGION_ID.forEach((regionId, areaCd) -> {
            regionRepository.findById(regionId).ifPresent(region -> {
                if (region.getApiAreaCd() == null || region.getApiAreaCd().isBlank()) {
                    region.setApiAreaCd(areaCd);
                    regionRepository.save(region);
                }
            });
        });
    }
}
