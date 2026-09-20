package com.tripping.backend.place.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripping.backend.global.config.CacheConfig;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// 한국관광공사 "관광 빅데이터 - 지역별 방문자수(DataLabService)" 연동.
// TourApiService와 마찬가지로 data-lab-api.service-key(디코딩 버전)를 그대로 사용한다.
//
// 👈 중요 1: metcoRegnVisitrDDList 오퍼레이션은 공식 매뉴얼상 요청 파라미터에 지역 필터가
// 아예 없다 (numOfRows/pageNo/MobileOS/MobileApp/serviceKey/_type/startYmd/endYmd 뿐).
// 즉 호출할 때마다 "전국 17개 시도 × 관광객구분(현지인/외지인/외국인) 3개"가 통째로 온다.
// 그래서 우리가 원하는 지역은 응답의 areaCode 필드로 직접 걸러내야 한다.
// (예전엔 요청에 areaCd= 를 붙였는데, 이건 API가 받지도 않는 파라미터라 에러 응답이 왔고,
// 그 에러 응답을 "데이터 없음"으로 조용히 처리해버려서 항상 0으로 나왔었음.)
//
// 👈 중요 2: 이 API 응답의 areaCode는 우리가 Region.apiAreaCd에 저장해둔 "관광공사 TourAPI
// areaCd" 체계(서울=1, 부산=6, 대구=4 ...)가 아니라, 행정안전부 표준 시도코드 체계
// (서울=11, 부산=26, 대구=27 ...)를 쓴다. 매뉴얼 응답 예제에 <areaCode>11</areaCode>가
// 서울로 나와있는 걸로 확인됨. 그래서 필터링 직전에 TourAPI areaCd -> 행안부 표준코드로
// 한 번 변환해줘야 한다.
//
// 👈 중요 3: 원본 데이터는 주 1회 주기로 갱신돼서 날짜가 듬성듬성하고, 최신 데이터도 약
// 5주 지연 공개된다. 그래서 "최근 N일" 같은 최근 구간은 실측 데이터가 거의 없을 수 있다.
// 이 서비스는 실측값(과 신뢰도를 판단할 rowCount)만 반환하고, "최근 구간을 어떻게 채울지"는
// InsightService.regionalVisitors()에서 작년 동기 데이터 기반 추정으로 처리한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class DataLabApiService {

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/DataLabService";
    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int MAX_PAGES = 50; // 안전장치 - 무한루프 방지
    private static final int RELIABLE_ROW_COUNT = 3; // touDivCd(현지인/외지인/외국인) 3개가 다 있어야 신뢰

    // 관광공사 TourAPI areaCd(우리 Region.apiAreaCd) -> 행정안전부 표준 시도코드(DataLabService 응답 areaCode).
    // ⚠️ 강원=51, 전북=52는 실제 API 응답 데이터로 검증 완료된 값. 수정하지 말 것.
    private static final Map<String, String> TOUR_AREA_CD_TO_DATALAB_AREA_CODE = Map.ofEntries(
            Map.entry("1", "11"),  // 서울
            Map.entry("2", "28"),  // 인천
            Map.entry("3", "30"),  // 대전
            Map.entry("4", "27"),  // 대구
            Map.entry("5", "29"),  // 광주
            Map.entry("6", "26"),  // 부산
            Map.entry("7", "31"),  // 울산
            Map.entry("8", "36"),  // 세종
            Map.entry("31", "41"), // 경기
            Map.entry("32", "51"), // 강원
            Map.entry("33", "43"), // 충북
            Map.entry("34", "44"), // 충남
            Map.entry("35", "47"), // 경북
            Map.entry("36", "48"), // 경남
            Map.entry("37", "52"), // 전북
            Map.entry("38", "46"), // 전남
            Map.entry("39", "50")  // 제주
    );

    // 위 매핑의 역방향 (행안부 표준코드 -> TourAPI areaCd). 전국을 한 번에 집계할 때,
    // 응답의 areaCode를 우리 Region.apiAreaCd 체계로 되돌리는 데 쓴다.
    private static final Map<String, String> DATALAB_AREA_CODE_TO_TOUR_AREA_CD = buildReverseMap();

    private static Map<String, String> buildReverseMap() {
        Map<String, String> reverse = new HashMap<>();
        TOUR_AREA_CD_TO_DATALAB_AREA_CODE.forEach((tourCd, dataLabCd) -> reverse.put(dataLabCd, tourCd));
        return Map.copyOf(reverse);
    }

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${data-lab-api.service-key}")
    private String serviceKey;

    // 날짜 하나에 대한 집계 결과. rowCount는 그 날짜에 실제로 합산된 touDivCd 행 개수
    // (현지인/외지인/외국인 최대 3개)로, 3 미만이면 일부 구분값이 누락된 신뢰할 수 없는
    // 날짜라는 뜻이다 (호출부인 InsightService에서 이 값으로 걸러낸다).
    public record DailyVisitorAggregate(long totalVisitors, int rowCount) {
    }

    /**
     * 시도 단위(areaCd, 관광공사 TourAPI 체계) 날짜 범위 내 일자별 총 방문자수
     * (현지인+외지인+외국인 합산)와 합산에 쓰인 행 개수를 함께 조회한다.
     * 실패하거나 데이터가 없으면 빈 Map을 반환한다.
     * <p>
     * 같은 (areaCd, start, end) 조합은 하루 동안 캐시된다 — 이 API는 요청 범위와 무관하게
     * 매번 전국 1년치(약 17,000건)를 반환해서 호출 비용이 크기 때문 (CacheConfig 참고).
     */
    @Cacheable(cacheNames = CacheConfig.DATA_LAB_DAILY_VISITORS_CACHE, key = "#areaCd + '_' + #start + '_' + #end")
    public Map<LocalDate, DailyVisitorAggregate> fetchDailyVisitorAggregates(String areaCd, LocalDate start, LocalDate end) {
        Map<LocalDate, Long> sums = new TreeMap<>();
        Map<LocalDate, Integer> counts = new TreeMap<>();
        Map<LocalDate, DailyVisitorAggregate> result = new TreeMap<>();
        if (areaCd == null || areaCd.isBlank()) {
            return result;
        }

        // DataLabService 응답은 행안부 표준 시도코드를 쓰므로 변환해서 비교해야 한다.
        String targetAreaCode = TOUR_AREA_CD_TO_DATALAB_AREA_CODE.getOrDefault(areaCd, areaCd);

        forEachItem(start, end, item -> {
            // 이 API는 지역 필터 없이 전국 데이터를 다 주므로, 우리가 원하는 지역만 골라낸다.
            String itemAreaCode = item.path("areaCode").asText(null);
            if (itemAreaCode == null || !itemAreaCode.equals(targetAreaCode)) {
                return;
            }
            LocalDate date = parseBaseYmd(item);
            if (date == null) {
                return;
            }
            // touDivCd(1=현지인/2=외지인/3=외국인)별로 행이 따로 오므로 날짜당 합산 + 행 개수 카운트.
            sums.merge(date, Math.round(item.path("touNum").asDouble(0)), Long::sum);
            counts.merge(date, 1, Integer::sum);
        }, "areaCd=" + areaCd);

        for (Map.Entry<LocalDate, Long> entry : sums.entrySet()) {
            LocalDate date = entry.getKey();
            result.put(date, new DailyVisitorAggregate(entry.getValue(), counts.getOrDefault(date, 0)));
        }
        return result;
    }

    /**
     * 전국 17개 시도의 기간 내 총 방문자수를 한 번의 조회로 모두 집계한다.
     * 대시보드 "지역별 인기" 랭킹처럼 지역끼리 비교해야 할 때 쓴다 —
     * 지역마다 fetchDailyVisitorAggregates()를 17번 부르는 대신, 어차피 전국이 통째로
     * 오는 응답을 한 번만 읽어서 areaCode별로 나눠 담는다.
     * <p>
     * 반환 키는 우리 Region.apiAreaCd(관광공사 TourAPI 체계)로 되돌려서 준다.
     * touDivCd 3종이 다 모이지 않은 날짜는 신뢰할 수 없어 합산에서 제외한다.
     */
    @Cacheable(cacheNames = CacheConfig.DATA_LAB_DAILY_VISITORS_CACHE, key = "'ALL_REGIONS_' + #start + '_' + #end")
    public Map<String, Long> fetchRegionTotals(LocalDate start, LocalDate end) {
        // areaCode -> (날짜 -> [합계, 행 개수])
        Map<String, Map<LocalDate, long[]>> perArea = new HashMap<>();

        forEachItem(start, end, item -> {
            String itemAreaCode = item.path("areaCode").asText(null);
            if (itemAreaCode == null) {
                return;
            }
            LocalDate date = parseBaseYmd(item);
            if (date == null) {
                return;
            }
            long[] slot = perArea
                    .computeIfAbsent(itemAreaCode, k -> new TreeMap<>())
                    .computeIfAbsent(date, k -> new long[2]);
            slot[0] += Math.round(item.path("touNum").asDouble(0));
            slot[1] += 1;
        }, "allRegions");

        Map<String, Long> totals = new HashMap<>();
        perArea.forEach((areaCode, byDate) -> {
            long sum = 0;
            for (long[] slot : byDate.values()) {
                if (slot[1] >= RELIABLE_ROW_COUNT) {
                    sum += slot[0];
                }
            }
            String tourAreaCd = DATALAB_AREA_CODE_TO_TOUR_AREA_CD.get(areaCode);
            if (tourAreaCd != null && sum > 0) {
                totals.put(tourAreaCd, sum);
            }
        });
        return totals;
    }

    // 페이지네이션을 돌면서 응답의 item 하나하나를 consumer에게 넘긴다.
    // 두 공개 메서드가 같은 호출/페이징 로직을 쓰되 집계 방식만 다르기 때문에 여기로 뺐다.
    private void forEachItem(LocalDate start, LocalDate end, Consumer<JsonNode> consumer, String logContext) {
        try {
            int numOfRows = 1000;
            int pageNo = 1;
            int totalCount = Integer.MAX_VALUE;
            int fetchedRows = 0;

            while (fetchedRows < totalCount && pageNo <= MAX_PAGES) {
                String encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
                String urlStr = BASE_URL + "/metcoRegnVisitrDDList"
                        + "?serviceKey=" + encodedKey
                        + "&MobileOS=ETC&MobileApp=TripPing"
                        + "&numOfRows=" + numOfRows + "&pageNo=" + pageNo + "&_type=json"
                        + "&startYmd=" + start.format(YMD)
                        + "&endYmd=" + end.format(YMD);

                String responseBody = restTemplate.getForObject(URI.create(urlStr), String.class);
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode response = root.path("response");

                String resultCode = response.path("header").path("resultCode").asText(null);
                if (resultCode != null && !resultCode.equals("0000")) {
                    log.warn("DataLabService(metcoRegnVisitrDDList) 응답 에러: resultCode={}, resultMsg={}",
                            resultCode, response.path("header").path("resultMsg").asText(""));
                    break;
                }

                JsonNode body = response.path("body");
                totalCount = body.path("totalCount").asInt(0);

                JsonNode itemNode = body.path("items").path("item");
                List<JsonNode> items = new ArrayList<>();
                if (itemNode.isArray()) {
                    itemNode.forEach(items::add);
                } else if (itemNode.isObject()) {
                    items.add(itemNode);
                }

                if (items.isEmpty()) {
                    break;
                }

                items.forEach(consumer);

                fetchedRows += items.size();
                pageNo++;
            }
        } catch (Exception e) {
            log.warn("DataLabService(metcoRegnVisitrDDList) 호출 실패: {}, error={}", logContext, e.getMessage());
        }
    }

    private LocalDate parseBaseYmd(JsonNode item) {
        String baseYmd = item.path("baseYmd").asText(null);
        if (baseYmd == null || baseYmd.length() != 8) {
            return null;
        }
        try {
            return LocalDate.parse(baseYmd, YMD);
        } catch (Exception e) {
            return null;
        }
    }
}
