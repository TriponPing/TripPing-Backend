package com.tripping.backend.place.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 공공데이터포털의 "Decoding(디코딩)" 버전 서비스키를 그대로 넣으면 돼요.
    // 코드에서 URLEncoder.encode()로 직접 인코딩해서 붙이기 때문에, 이미 인코딩된
    // "Encoding" 버전 키를 넣으면 이중 인코딩되어 실패해요.
    @Value("${tour-api.service-key}")
    private String serviceKey;

    @Value("${tour-api.base-url}")
    private String baseUrl;

    /** 장소 이름으로 검색해서 가장 그럴듯한(첫 번째) contentId 하나 찾음. 못 찾으면 null. */
    public String findContentId(String keyword) {
        try {
            String encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String urlStr = baseUrl + "/searchKeyword2"
                    + "?serviceKey=" + encodedKey
                    + "&keyword=" + encodedKeyword
                    + "&MobileOS=ETC&MobileApp=TripPing&numOfRows=1&_type=json";

            String responseBody = restTemplate.getForObject(URI.create(urlStr), String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");

            if (itemNode.isArray() && !itemNode.isEmpty()) {
                return itemNode.get(0).path("contentid").asText(null);
            } else if (itemNode.isObject()) {
                return itemNode.path("contentid").asText(null);
            }
            return null;
        } catch (Exception e) {
            log.warn("TourAPI searchKeyword2 실패: keyword={}, error={}", keyword, e.getMessage());
            return null;
        }
    }

    /** contentId로 상세 설명(overview) 가져옴. 없으면 null. */
    public String fetchOverview(String contentId) {
        try {
            String encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
            String urlStr = baseUrl + "/detailCommon2"
                    + "?serviceKey=" + encodedKey
                    + "&contentId=" + contentId
                    + "&MobileOS=ETC&MobileApp=TripPing"
                    + "&_type=json";

            String responseBody = restTemplate.getForObject(URI.create(urlStr), String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            log.info("TourAPI 응답 원문: {}", responseBody);
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");
            JsonNode target = itemNode.isArray() ? itemNode.get(0) : itemNode;

            if (target == null || target.isMissingNode()) {
                return null;
            }
            String overview = target.path("overview").asText(null);
            return (overview != null && !overview.isBlank()) ? overview : null;
        } catch (Exception e) {
            log.warn("TourAPI detailCommon2 실패: contentId={}, error={}", contentId, e.getMessage());
            return null;
        }
    }
}