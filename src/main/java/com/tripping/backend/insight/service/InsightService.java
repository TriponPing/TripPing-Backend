package com.tripping.backend.insight.service;

import com.tripping.backend.entity.Region;
import com.tripping.backend.insight.dto.DailyVisitResponse;
import com.tripping.backend.insight.dto.RegionalVisitorResponse;
import com.tripping.backend.insight.dto.RouteRankingResponse;
import com.tripping.backend.insight.dto.SpotEvidenceResponse;
import com.tripping.backend.insight.dto.TrendsSummaryResponse;
import com.tripping.backend.insight.repository.DailyVisitProjection;
import com.tripping.backend.insight.repository.InsightRegionRepository;
import com.tripping.backend.insight.repository.InsightRouteRepository;
import com.tripping.backend.insight.repository.RouteCountProjection;
import com.tripping.backend.insight.repository.SpotEvidenceRepository;
import com.tripping.backend.place.service.DataLabApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class InsightService {

    private static final int MAX_SAMPLE_COMMENTS = 3;

    private final InsightRouteRepository insightRouteRepository;
    private final SpotEvidenceRepository spotEvidenceRepository;
    private final InsightRegionRepository insightRegionRepository;
    private final DataLabApiService dataLabApiService;

    // range 계산(period+endDate 조합인지, 달력에서 고른 startDate~endDate인지)은
    // 컨트롤러가 이미 끝내서 넘겨준다 — 여기는 그 구간으로 집계만 한다.
    public TrendsSummaryResponse summary(String region, DateRange current) {
        DateRange previous = current.previous();
        String regionName = normalizeRegion(region);

        long currentTotal = nullToZero(insightRouteRepository.countVisits(current.start(), current.end(), regionName));
        long previousTotal = nullToZero(insightRouteRepository.countVisits(previous.start(), previous.end(), regionName));

        return new TrendsSummaryResponse(currentTotal, round1(percentChange(currentTotal, previousTotal)));
    }

    public List<RouteRankingResponse> risingRoutes(String region, DateRange current) {
        DateRange previous = current.previous();
        String regionName = normalizeRegion(region);

        List<RouteCountProjection> currentRoutes = insightRouteRepository.findTopRoutes(current.start(), current.end(), regionName);
        List<RouteCountProjection> previousRoutes = insightRouteRepository.findTopRoutes(previous.start(), previous.end(), regionName);

        Map<String, Long> previousCounts = previousRoutes.stream()
                .collect(Collectors.toMap(RouteCountProjection::getRouteName, RouteCountProjection::getVisitCount, (a, b) -> a));

        return currentRoutes.stream()
                .map(row -> {
                    long previousCount = previousCounts.getOrDefault(row.getRouteName(), 0L);
                    double changeRate = round1(percentChange(row.getVisitCount(), previousCount));
                    return new RouteRankingResponse(row.getRouteName(), row.getVisitCount(), changeRate, parseSpotIds(row.getSpotIds()));
                })
                .sorted(Comparator.comparingLong(RouteRankingResponse::getVisitCount).reversed())
                .toList();
    }

    // "12,45,7" -> [12, 45, 7]. 빈 값이 오는 일은 없지만(루트는 항상 spot 1개 이상으로
    // 이뤄짐) 방어적으로 null·빈 문자열은 빈 리스트로 처리한다.
    private List<Long> parseSpotIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(Long::parseLong).toList();
    }

    // "일자별 이동량" 차트용 - summary()와 같은 DateRange.forPeriod()로 기간을 구하지만,
    // 직전 기간과 비교하지 않고 선택한 기간 안의 날짜별 건수만 반환한다.
    // 방문 기록이 없는 날짜도 차트에서 끊기지 않도록 0건으로 채워서 모든 날짜를 다 내려준다.
    public List<DailyVisitResponse> dailyVisits(String period, String region) {
        DateRange current = DateRange.forPeriod(period, LocalDate.now());
        String regionName = normalizeRegion(region);

        List<DailyVisitProjection> rows = insightRouteRepository.countVisitsByDay(current.start(), current.end(), regionName);
        Map<LocalDate, Long> countsByDate = rows.stream()
                .collect(Collectors.toMap(DailyVisitProjection::getVisitDate, DailyVisitProjection::getVisitCount));

        List<DailyVisitResponse> result = new ArrayList<>();
        for (LocalDate date = current.start(); !date.isAfter(current.end()); date = date.plusDays(1)) {
            result.add(new DailyVisitResponse(date, countsByDate.getOrDefault(date, 0L)));
        }
        return result;
    }

    // "상품 기획안" 보고서의 데이터 근거용 - 주어진 관광지들에 실제로 쌓인 평점·후기를
    // 있는 그대로 집계한다. 기간·지역으로 거르지 않는다(만족도는 트렌드처럼 특정 구간의
    // "변화"가 아니라 그 장소 자체의 누적 품질 신호라서, PlaceDetail의 averageRating과
    // 같은 방식으로 전체 기간을 본다.
    public SpotEvidenceResponse spotEvidence(List<Long> spotIds) {
        if (spotIds == null || spotIds.isEmpty()) {
            return new SpotEvidenceResponse(null, 0, List.of());
        }
        SpotEvidenceRepository.RatingRow rating = spotEvidenceRepository.findAverageRating(spotIds);
        Double average = rating == null || rating.getAverageRating() == null ? null : round1(rating.getAverageRating());
        long count = rating == null || rating.getRatingCount() == null ? 0 : rating.getRatingCount();
        List<String> comments = spotEvidenceRepository.findRecentComments(spotIds, MAX_SAMPLE_COMMENTS);
        return new SpotEvidenceResponse(average, count, comments);
    }

    // [일자별 이동량 차트 참고선] 한국관광공사 "빅데이터 지역별 방문자수(DataLabService)" 기준,
    // 선택한 지역(시도)의 실제 방문자 규모. 우리 자체 방문 핑 데이터가 아직 적어서(콜드스타트),
    // 국가 통계 기준 실제 방문자 흐름을 옆에 같이 보여주기 위한 용도.
    // "전체 지역"이거나 매핑된 areaCd가 없는 지역이면 빈 리스트를 반환한다(프론트에서 참고선 숨김).
    public List<RegionalVisitorResponse> regionalVisitors(String period, String region) {
        String regionName = normalizeRegion(region);
        if (regionName == null) {
            return List.of();
        }

        String areaCd = insightRegionRepository.findByRegionName(regionName)
                .map(Region::getApiAreaCd)
                .orElse(null);
        if (areaCd == null || areaCd.isBlank()) {
            return List.of();
        }

        DateRange current = DateRange.forPeriod(period, LocalDate.now());
        Map<LocalDate, Long> visitorsByDate = dataLabApiService.fetchDailyVisitors(areaCd, current.start(), current.end());

        List<RegionalVisitorResponse> result = new ArrayList<>();
        for (LocalDate date = current.start(); !date.isAfter(current.end()); date = date.plusDays(1)) {
            result.add(new RegionalVisitorResponse(date, visitorsByDate.getOrDefault(date, 0L)));
        }
        return result;
    }

    // 프론트의 "전체 지역"은 지역 필터 없음(null)으로 취급.
    private String normalizeRegion(String region) {
        if (region == null || region.isBlank() || region.equals("전체 지역")) return null;
        return region;
    }

    private double percentChange(long current, long previous) {
        if (previous == 0) return current == 0 ? 0.0 : 100.0;
        return ((double) (current - previous) / previous) * 100.0;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
