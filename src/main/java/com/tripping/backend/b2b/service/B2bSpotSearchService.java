package com.tripping.backend.b2b.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripping.backend.b2b.dto.SpotSearchResponse;
import com.tripping.backend.b2b.repository.B2bTouristSpotRepository;
import com.tripping.backend.entity.TouristSpot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

// 상품 일정에 넣을 관광지를 찾는다.
//
// 우리 tourist_spot에는 아직 일부 지역만 들어 있어서, 그것만 뒤지면 울산·경주
// 같은 지역은 아무것도 안 나온다. 그래서 한국관광공사 TourAPI도 함께 찾고,
// 담당자가 실제로 고른 관광지만 tourist_spot에 등록한다. 검색만으로 DB가
// 불어나지 않고, 일정에는 spot_id로 저장되므로 그대로 저장된다.
@Slf4j
@RequiredArgsConstructor
@Service
public class B2bSpotSearchService {

    // tourist_spot.category는 앱에서 쓰는 attraction/restaurant/cafe 세 값으로
    // 굳어져 있다(PopularTripService·TouristSpotService·RouteRecommendService가
    // 이 값으로 분기한다). 관광공사 분류를 그대로 넣으면 그 분기에서 전부
    // 빠지므로 여기서 세 값으로 맞춘다.
    private static final Map<String, String> CONTENT_TYPE_TO_CATEGORY = Map.of(
            "12", "attraction",   // 관광지
            "14", "attraction",   // 문화시설
            "15", "attraction",   // 축제공연행사
            "28", "attraction",   // 레포츠
            "39", "restaurant"    // 음식점 — 카페는 아래 cat3로 따로 가른다
            // 32(숙박) · 38(쇼핑)은 대응하는 값이 없어 분류 없이 둔다.
    );

    // 관광공사에는 카페가 별도 분류로 없고 음식점(39) 안에 소분류로 들어 있다.
    private static final String CAT3_CAFE = "A05020900"; // 카페·전통찻집

    private String categoryOf(JsonNode item) {
        String category = CONTENT_TYPE_TO_CATEGORY.get(text(item, "contenttypeid"));
        if ("restaurant".equals(category) && CAT3_CAFE.equals(text(item, "cat3"))) {
            return "cafe";
        }
        return category;
    }

    // TourAPI 지역코드 → 우리 region 테이블 코드.
    private static final Map<String, String> AREA_TO_REGION = Map.ofEntries(
            Map.entry("1", "R01"), Map.entry("2", "R06"), Map.entry("3", "R09"),
            Map.entry("4", "R07"), Map.entry("5", "R08"), Map.entry("6", "R02"),
            Map.entry("7", "R10"), Map.entry("8", "R11"), Map.entry("31", "R05"),
            Map.entry("32", "R03"), Map.entry("33", "R12"), Map.entry("34", "R13"),
            Map.entry("35", "R16"), Map.entry("36", "R17"), Map.entry("37", "R14"),
            Map.entry("38", "R15"), Map.entry("39", "R04")
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final B2bTouristSpotRepository touristSpotRepository;

    @Value("${tour-api.service-key}")
    private String serviceKey;

    @Value("${tour-api.base-url}")
    private String baseUrl;

    @Transactional(readOnly = true)
    public List<SpotSearchResponse> search(String query) {
        String keyword = query == null ? "" : query.trim();
        if (keyword.length() < 2) return List.of();

        List<SpotSearchResponse> results = new ArrayList<>();
        Set<String> seenContentIds = new HashSet<>();
        Set<String> seenNames = new HashSet<>();

        // 이미 등록된 관광지를 먼저 보여준다. 담을 때 추가 등록이 필요 없다.
        for (TouristSpot spot : touristSpotRepository
                .findTop20ByNameContainingIgnoreCaseOrderByNameAsc(keyword)) {
            results.add(SpotSearchResponse.saved(spot));
            if (spot.getApiContentId() != null) seenContentIds.add(spot.getApiContentId());
            seenNames.add(spot.getName());
        }

        // 나머지는 관광공사에서 찾는다. 아직 우리 DB에 없는 것만 후보로 올린다.
        for (JsonNode item : fetchFromTourApi(keyword)) {
            String contentId = text(item, "contentid");
            String name = text(item, "title");
            if (contentId == null || name == null) continue;
            if (!seenContentIds.add(contentId) || !seenNames.add(name)) continue;

            results.add(SpotSearchResponse.builder()
                    .spotId(null)
                    .contentId(contentId)
                    .name(name)
                    .address(text(item, "addr1"))
                    .category(categoryOf(item))
                    .latitude(decimal(item, "mapy"))
                    .longitude(decimal(item, "mapx"))
                    .imageUrl(text(item, "firstimage"))
                    .registered(false)
                    .build());
        }

        return results;
    }

    // 검색 결과에서 고른 관광지를 등록하고 spot_id를 돌려준다.
    // 이미 등록돼 있으면 그 관광지를 그대로 쓴다.
    @Transactional
    public SpotSearchResponse register(String contentId) {
        if (contentId == null || contentId.isBlank()) {
            throw new IllegalArgumentException("관광지를 다시 선택해주세요.");
        }

        Optional<TouristSpot> existing = touristSpotRepository.findByApiContentId(contentId);
        if (existing.isPresent()) return SpotSearchResponse.saved(existing.get());

        JsonNode item = fetchDetail(contentId);
        if (item == null) {
            throw new NoSuchElementException("관광공사에서 이 관광지를 찾지 못했습니다.");
        }

        TouristSpot spot = touristSpotRepository.save(TouristSpot.builder()
                .apiContentId(contentId)
                .name(cut(text(item, "title"), 100))
                .category(categoryOf(item))
                .regionId(AREA_TO_REGION.get(text(item, "areacode")))
                .address(cut(text(item, "addr1"), 255))
                .latitude(decimal(item, "mapy"))
                .longitude(decimal(item, "mapx"))
                .imageUrl(cut(text(item, "firstimage"), 255))
                .build());

        log.info("TourAPI 관광지 등록: contentId={} name={}", contentId, spot.getName());
        return SpotSearchResponse.saved(spot);
    }

    private List<JsonNode> fetchFromTourApi(String keyword) {
        String url = baseUrl + "/searchKeyword2"
                + "?serviceKey=" + encode(serviceKey)
                + "&keyword=" + encode(keyword)
                + "&MobileOS=ETC&MobileApp=TripPing&_type=json"
                + "&numOfRows=20&pageNo=1";
        return items(url);
    }

    private JsonNode fetchDetail(String contentId) {
        String url = baseUrl + "/detailCommon2"
                + "?serviceKey=" + encode(serviceKey)
                + "&contentId=" + encode(contentId)
                + "&MobileOS=ETC&MobileApp=TripPing&_type=json"
                + "&numOfRows=1&pageNo=1";
        List<JsonNode> found = items(url);
        return found.isEmpty() ? null : found.get(0);
    }

    private List<JsonNode> items(String url) {
        try {
            JsonNode root = objectMapper.readTree(
                    restTemplate.getForObject(URI.create(url), String.class));
            JsonNode item = root.path("response").path("body").path("items").path("item");
            if (item.isArray()) {
                List<JsonNode> list = new ArrayList<>();
                item.forEach(list::add);
                return list;
            }
            return item.isObject() ? List.of(item) : List.of();
        } catch (Exception e) {
            // 관광공사가 느리거나 막혀도 우리 DB 결과는 그대로 보여준다.
            log.warn("TourAPI 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return (value == null || value.isBlank()) ? null : value;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String cut(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
