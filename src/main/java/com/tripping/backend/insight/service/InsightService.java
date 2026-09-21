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
import java.util.TreeMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class InsightService {

    // ---- 지역별 이동량 변화(regionalVisitors) 추정 로직 상수 ----
    // DataLab 실측 데이터는 약 5주 지연 공개돼서 "최근 N일" 구간은 실측값이 거의 없다.
    // 그래서 작년 동기 데이터를 보정계수로 스케일링해 최근 구간을 추정해서 채운다.
    private static final int LOOKBACK_DAYS = 400;        // DataLab 조회 시 요청 시작일보다 얼마나 더 과거까지 받아둘지
    private static final int YEAR_OVER_YEAR_DAYS = 364;  // 52주×7일 - 요일이 정확히 맞도록 365가 아닌 364 사용
    private static final int COMPARE_WINDOW_DAYS = 28;   // 보정계수 계산에 쓰는 "최근 4주" 길이
    private static final int NEAR_MATCH_TOLERANCE_DAYS = 3; // 작년 동기 날짜에 실측값이 없을 때 허용하는 오차
    private static final int SMOOTHING_RADIUS_DAYS = 3;  // 7일 중심 이동평균(앞뒤 3일)
    private static final double MIN_VALID_FACTOR = 0.5;
    private static final double MAX_VALID_FACTOR = 2.0;
    private static final int MIN_RELIABLE_ROW_COUNT = 3; // touDivCd(현지인/외지인/외국인) 3개가 다 있어야 신뢰

    // ---- 상품 기획안 보고서 근거(spotEvidence)용 ----
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

    // [지역별 이동량 변화 차트] 한국관광공사 "빅데이터 지역별 방문자수(DataLabService)" 기준,
    // 선택한 지역(시도)의 실제 방문자 규모를 참고선으로 보여준다. 우리 자체 방문 핑 데이터가
    // 아직 적어서(콜드스타트) + DataLab 실측 데이터가 약 5주 지연 공개돼서 "최근 N일" 구간은
    // 실측값이 부족하다. 그래서 없는 날짜는 작년 동기(364일 전, 요일 정렬 목적) 데이터에
    // 최근 4주 기준 보정계수를 곱해 추정하고, 마지막에 7일 이동평균으로 평활한다.
    // "전체 지역"이면 전국 17개 시도를 합산한 전국 이동량을 내려준다.
    // 특정 지역인데 관광공사 지역코드 매핑이 없으면 빈 리스트를 반환한다(프론트에서 참고선 숨김).
    public List<RegionalVisitorResponse> regionalVisitors(String period, String region) {
        String regionName = normalizeRegion(region);

        String areaCd = null;
        if (regionName != null) {
            areaCd = insightRegionRepository.findByRegionName(regionName)
                    .map(Region::getApiAreaCd)
                    .orElse(null);
            if (areaCd == null || areaCd.isBlank()) {
                return List.of();
            }
        }

        DateRange current = DateRange.forPeriod(period, LocalDate.now());

        // 1) 데이터 확보 — 전국을 하루에 한 번만 받아서 캐시하고 여기서 필요한 만큼 잘라 쓴다.
        // 이 API는 요청 범위와 무관하게 매번 전국치를 통째로 주기 때문에, 지역·기간별로
        // 따로 부르면 같은 응답을 반복해서 받게 된다(화면이 몇 초씩 멈추던 원인).
        Map<String, Map<LocalDate, DataLabApiService.DailyVisitorAggregate>> byRegion =
                dataLabApiService.fetchAllRegionDailyAggregates();

        // 2) 비정상 날짜 제거 — touDivCd(현지인/외지인/외국인) 3개가 다 모이지 않은 날짜는 제외.
        TreeMap<LocalDate, Long> actualByDate = areaCd == null
                ? sumAllRegions(byRegion)
                : reliableOnly(byRegion.getOrDefault(areaCd, Map.of()));

        // 3) 보정계수 계산 (최근 4주 vs 작년 같은 4주)
        double correctionFactor = calculateCorrectionFactor(actualByDate);

        // 4) 날짜별 추정값(평활 전) 생성
        Map<LocalDate, Double> rawByDate = new TreeMap<>();
        Map<LocalDate, Boolean> estimatedByDate = new TreeMap<>();
        for (LocalDate date = current.start(); !date.isAfter(current.end()); date = date.plusDays(1)) {
            Long actual = actualByDate.get(date);
            if (actual != null) {
                rawByDate.put(date, actual.doubleValue());
                estimatedByDate.put(date, false);
                continue;
            }
            Long baseValue = findNearestActualValue(actualByDate, date.minusDays(YEAR_OVER_YEAR_DAYS));
            rawByDate.put(date, baseValue == null ? null : baseValue * correctionFactor);
            estimatedByDate.put(date, true);
        }

        // 5) 7일 중심 이동평균 평활 (앞뒤 3일, 양 끝은 사용 가능한 값만으로 평균)
        List<LocalDate> orderedDates = new ArrayList<>(rawByDate.keySet());
        List<RegionalVisitorResponse> result = new ArrayList<>();
        for (int i = 0; i < orderedDates.size(); i++) {
            LocalDate date = orderedDates.get(i);
            double sum = 0;
            int count = 0;
            for (int offset = -SMOOTHING_RADIUS_DAYS; offset <= SMOOTHING_RADIUS_DAYS; offset++) {
                int idx = i + offset;
                if (idx < 0 || idx >= orderedDates.size()) {
                    continue;
                }
                Double value = rawByDate.get(orderedDates.get(idx));
                if (value == null) {
                    continue;
                }
                sum += value;
                count++;
            }
            long smoothed = count == 0 ? 0L : Math.round(sum / count);
            result.add(new RegionalVisitorResponse(date, smoothed, estimatedByDate.get(date)));
        }
        return result;
    }

    // 한 지역의 일자별 집계에서 신뢰할 수 있는 날짜만 남긴다.
    private TreeMap<LocalDate, Long> reliableOnly(Map<LocalDate, DataLabApiService.DailyVisitorAggregate> aggregates) {
        TreeMap<LocalDate, Long> actualByDate = new TreeMap<>();
        for (Map.Entry<LocalDate, DataLabApiService.DailyVisitorAggregate> entry : aggregates.entrySet()) {
            if (entry.getValue().rowCount() >= MIN_RELIABLE_ROW_COUNT) {
                actualByDate.put(entry.getKey(), entry.getValue().totalVisitors());
            }
        }
        return actualByDate;
    }

    // "전체 지역"일 때 쓰는 전국 합계. 날짜별로 신뢰할 수 있는 시도들의 값만 더한다.
    // 일부 시도의 데이터가 빠진 날짜는 전국 합계도 과소 집계되므로, 그날 집계에 들어간
    // 시도 수가 너무 적으면(절반 미만) 아예 실측으로 치지 않고 추정에 맡긴다.
    private TreeMap<LocalDate, Long> sumAllRegions(
            Map<String, Map<LocalDate, DataLabApiService.DailyVisitorAggregate>> byRegion) {
        TreeMap<LocalDate, Long> sums = new TreeMap<>();
        TreeMap<LocalDate, Integer> regionCounts = new TreeMap<>();

        for (Map<LocalDate, DataLabApiService.DailyVisitorAggregate> aggregates : byRegion.values()) {
            for (Map.Entry<LocalDate, DataLabApiService.DailyVisitorAggregate> entry : aggregates.entrySet()) {
                if (entry.getValue().rowCount() < MIN_RELIABLE_ROW_COUNT) {
                    continue;
                }
                sums.merge(entry.getKey(), entry.getValue().totalVisitors(), Long::sum);
                regionCounts.merge(entry.getKey(), 1, Integer::sum);
            }
        }

        int minRegions = Math.max(1, byRegion.size() / 2);
        TreeMap<LocalDate, Long> actualByDate = new TreeMap<>();
        sums.forEach((date, total) -> {
            if (regionCounts.getOrDefault(date, 0) >= minRegions) {
                actualByDate.put(date, total);
            }
        });
        return actualByDate;
    }

    // 최근 4주(compareWindow) 실측 합계 ÷ 작년 같은 4주 실측 합계. 요일 정렬을 위해 364일 전을
    // 기준으로 삼는다. 어느 한쪽 합계라도 0이거나 계수가 0.5~2.0 범위를 벗어나면 이상치로 보고
    // 1.0(보정 없음)으로 고정한다.
    private double calculateCorrectionFactor(TreeMap<LocalDate, Long> actualByDate) {
        if (actualByDate.isEmpty()) {
            return 1.0;
        }
        LocalDate lastActual = actualByDate.lastKey();
        LocalDate compareStart = lastActual.minusDays(COMPARE_WINDOW_DAYS - 1);

        long thisYearSum = sumBetween(actualByDate, compareStart, lastActual);
        long lastYearSum = sumBetween(actualByDate, compareStart.minusDays(YEAR_OVER_YEAR_DAYS), lastActual.minusDays(YEAR_OVER_YEAR_DAYS));

        if (thisYearSum <= 0 || lastYearSum <= 0) {
            return 1.0;
        }
        double factor = (double) thisYearSum / lastYearSum;
        if (factor < MIN_VALID_FACTOR || factor > MAX_VALID_FACTOR) {
            return 1.0;
        }
        return factor;
    }

    private long sumBetween(Map<LocalDate, Long> byDate, LocalDate start, LocalDate end) {
        long sum = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Long value = byDate.get(date);
            if (value != null) {
                sum += value;
            }
        }
        return sum;
    }

    // base(작년 동기) 날짜에 실측값이 없으면 ±3일 이내에서 가장 가까운 날짜의 값을 대신 쓴다.
    // 거리가 같으면 이전 날짜를 우선한다. 그래도 없으면 null(추정 불가).
    private Long findNearestActualValue(Map<LocalDate, Long> actualByDate, LocalDate base) {
        Long exact = actualByDate.get(base);
        if (exact != null) {
            return exact;
        }
        for (int offset = 1; offset <= NEAR_MATCH_TOLERANCE_DAYS; offset++) {
            Long before = actualByDate.get(base.minusDays(offset));
            if (before != null) {
                return before;
            }
            Long after = actualByDate.get(base.plusDays(offset));
            if (after != null) {
                return after;
            }
        }
        return null;
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
