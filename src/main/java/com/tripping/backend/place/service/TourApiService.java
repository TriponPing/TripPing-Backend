package com.tripping.backend.place.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Slf4j
@Service
@RequiredArgsConstructor
public class TourApiService {

    private final RestTemplate restTemplate;

    @Value("${tour-api.service-key}")
    private String serviceKey;

    @Value("${tour-api.base-url}")
    private String baseUrl;

    /** 장소 이름으로 검색해서 가장 그럴듯한(첫 번째) contentId 하나 찾음. 못 찾으면 null. */
    public String findContentId(String keyword) {
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/searchKeyword2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("keyword", keyword)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "TripPing")
                    .queryParam("numOfRows", 1)
                    .queryParam("_type", "json")
                    .build()
                    .toUriString();

            JsonNode root = restTemplate.getForObject(url, JsonNode.class);
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
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/detailCommon2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("contentId", contentId)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "TripPing")
                    .queryParam("defaultYN", "Y")
                    .queryParam("overviewYN", "Y")
                    .queryParam("_type", "json")
                    .build()
                    .toUriString();

            JsonNode root = restTemplate.getForObject(url, JsonNode.class);
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