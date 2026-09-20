package com.tripping.backend.place.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// 한국관광공사 "관광 빅데이터 - 지역별 방문자수(DataLabService)" 연동.
// TourApiService와 마찬가지로 tour-api.service-key(디코딩 버전)를 그대로 재사용한다 -
// data.go.kr은 계정 단위로 키가 하나라, 관광공사(B551011) 산하 API는 활용신청만 승인되면
// 전부 같은 키로 호출 가능.
@Slf4j
@Service
@RequiredArgsConstructor
public class DataLabApiService {

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/DataLabService";
    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${tour-api.service-key}")
    private String serviceKey;

    /**
     * 시도 단위(areaCd) 날짜 범위 내 일자별 총 방문자수(현지인+외지인+외국인 합산)를 조회한다.
     * 실패하거나 데이터가 없으면 빈 Map을 반환한다 (호출부에서 0으로 취급하면 됨).
     */
    public Map<LocalDate, Long> fetchDailyVisitors(String areaCd, LocalDate start, LocalDate end) {
        Map<LocalDate, Long> result = new TreeMap<>();
        if (areaCd == null || areaCd.isBlank()) {
            return result;
        }

        try {
            String encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
            String urlStr = BASE_URL + "/metcoRegnVisitrDDList"
                    + "?serviceKey=" + encodedKey
                    + "&MobileOS=ETC&MobileApp=TripPing"
                    + "&numOfRows=1000&pageNo=1&_type=json"
                    + "&areaCd=" + URLEncoder.encode(areaCd, StandardCharsets.UTF_8)
                    + "&startYmd=" + start.format(YMD)
                    + "&endYmd=" + end.format(YMD);

            String responseBody = restTemplate.getForObject(URI.create(urlStr), String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");

            List<JsonNode> items = new ArrayList<>();
            if (itemNode.isArray()) {
                itemNode.forEach(items::add);
            } else if (itemNode.isObject()) {
                items.add(itemNode);
            }

            for (JsonNode item : items) {
                String baseYmd = item.path("baseYmd").asText(null);
                double touNum = item.path("touNum").asDouble(0);
                if (baseYmd == null || baseYmd.length() != 8) {
                    continue;
                }
                LocalDate date = LocalDate.parse(baseYmd, YMD);
                // touDivCd(1=현지인/2=외지인/3=외국인)별로 행이 따로 오므로 날짜당 합산.
                result.merge(date, Math.round(touNum), Long::sum);
            }
        } catch (Exception e) {
            log.warn("DataLabService(metcoRegnVisitrDDList) 호출 실패: areaCd={}, error={}", areaCd, e.getMessage());
        }
        return result;
    }
}
