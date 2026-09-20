package com.tripping.backend.insight.service;

import com.tripping.backend.insight.dto.DashboardSummaryResponse;
import com.tripping.backend.insight.dto.RegionRankResponse;
import com.tripping.backend.insight.dto.RouteNetworkResponse;
import com.tripping.backend.insight.repository.DashboardRepository;
import com.tripping.backend.insight.repository.InsightRouteRepository;
import com.tripping.backend.place.service.DataLabApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// B2B 대시보드 화면 전용 집계 서비스.
//
// InsightService(트렌드 화면)와 파일을 나눈 이유는 두 가지다 —
//  1) 관심사가 다르다: 트렌드는 "기간별 변화", 대시보드는 "현재 상태의 전체 그림".
//  2) 두 화면을 서로 다른 사람이 동시에 수정하는 일이 잦아서 한 파일에 두면 충돌이 난다.
// 단, "총 방문 핑"의 집계 기준만은 반드시 같아야 해서 InsightRouteRepository.countVisits()를
// 그대로 재사용한다 — 같은 기간을 보면 두 화면의 숫자가 항상 일치한다.
@RequiredArgsConstructor
@Service
public class DashboardService {

    private static final int MAX_NETWORK_NODES = 30;
    private static final int MAX_NETWORK_EDGES = 60;

    private final DashboardRepository dashboardRepository;
    private final InsightRouteRepository insightRouteRepository;
    private final DataLabApiService dataLabApiService;

    // 대시보드 상단 KPI 카드. 각 지표마다 직전 동일 길이 구간과 비교해 증감률을 함께 낸다.
    public DashboardSummaryResponse summary(String region, DateRange current) {
        DateRange previous = current.previous();
        String regionName = normalizeRegion(region);

        long currentVisits = nullToZero(insightRouteRepository.countVisits(current.start(), current.end(), regionName));
        long previousVisits = nullToZero(insightRouteRepository.countVisits(previous.start(), previous.end(), regionName));

        long currentTravelers = nullToZero(dashboardRepository.countActiveTravelers(current.start(), current.end(), regionName));
        long previousTravelers = nullToZero(dashboardRepository.countActiveTravelers(previous.start(), previous.end(), regionName));

        long currentRoutes = nullToZero(dashboardRepository.countRoutes(current.start(), current.end(), regionName));
        long previousRoutes = nullToZero(dashboardRepository.countRoutes(previous.start(), previous.end(), regionName));

        // 여행당 평균 방문 관광지 수. 시간 데이터를 쓸 수 없어서(핑 시각은 사용자가 임의로
        // 찍는 값이라 도착/출발 의미가 없음) 체류 시간 대신 쓰는 지표다.
        double currentAvgSpots = ratio(currentVisits, currentRoutes);
        double previousAvgSpots = ratio(previousVisits, previousRoutes);

        DashboardRepository.RatingRow rating =
                dashboardRepository.findAverageRating(current.start(), current.end(), regionName);
        Double averageRating = (rating == null || rating.getAverageRating() == null)
                ? null : round1(rating.getAverageRating());
        long ratingCount = (rating == null || rating.getRatingCount() == null) ? 0 : rating.getRatingCount();

        return new DashboardSummaryResponse(
                currentVisits, round1(percentChange(currentVisits, previousVisits)),
                currentTravelers, round1(percentChange(currentTravelers, previousTravelers)),
                currentRoutes, round1(percentChange(currentRoutes, previousRoutes)),
                round1(currentAvgSpots), round1(percentChange(currentAvgSpots, previousAvgSpots)),
                averageRating, ratingCount
        );
    }

    // "지역별 인기" 패널. 우리 자체 방문 핑과 관광공사 DataLab 방문자수를 시도별로 나란히 낸다.
    // 정렬 기준은 관광공사 방문자수 — 자체 핑은 아직 대부분 0이라 그것만으로는 순위가
    // 만들어지지 않기 때문. 관광공사 조회가 실패하면 자체 핑 순으로 정렬한다(화면은 계속 뜬다).
    public List<RegionRankResponse> regionRanking(DateRange current) {
        // 1) 우리 자체 핑을 시도별로 집계 (핑이 없는 지역은 행 자체가 없음)
        Map<String, Long> pingsByRegionName = new java.util.HashMap<>();
        for (DashboardRepository.RegionPingRow row : dashboardRepository.countPingsByRegion(current.start(), current.end())) {
            pingsByRegionName.merge(row.getRegionName(), nullToZero(row.getVisitCount()), Long::sum);
        }

        // 2) 관광공사 기준 지역별 총 방문자수 (전국이 한 번에 오므로 호출은 1회)
        Map<String, Long> visitorsByAreaCd = dataLabApiService.fetchRegionTotals(current.start(), current.end());

        // 3) 관광공사 지역코드가 매핑된 시도 목록을 기준으로 두 값을 합친다.
        //    핑이 0인 지역도 빠지지 않고 다 나온다.
        List<RegionRankResponse> result = new ArrayList<>();
        for (DashboardRepository.RegionCodeRow region : dashboardRepository.findMappedRegions()) {
            long pings = pingsByRegionName.getOrDefault(region.getRegionName(), 0L);
            Long visitors = visitorsByAreaCd.get(region.getAreaCd());
            result.add(new RegionRankResponse(region.getRegionName(), pings, visitors));
        }

        result.sort(Comparator
                .comparingLong((RegionRankResponse r) -> r.getRegionVisitors() == null ? -1L : r.getRegionVisitors())
                .thenComparingLong(RegionRankResponse::getVisitPings)
                .reversed());
        return result;
    }

    // "여행 루트 네트워크" 지도. 실제 방문 기록에서 노드(관광지)와 엣지(이어서 방문한 쌍)를 뽑는다.
    // 좌표가 없는 노드는 지도에 찍을 수 없으므로 제외하고, 그 노드에 붙은 엣지도 같이 버린다.
    public RouteNetworkResponse routeNetwork(String region, DateRange current) {
        String regionName = normalizeRegion(region);

        List<RouteNetworkResponse.Node> nodes = new ArrayList<>();
        java.util.Set<Long> plottableSpotIds = new java.util.HashSet<>();
        for (DashboardRepository.NetworkNodeRow row :
                dashboardRepository.findNetworkNodes(current.start(), current.end(), regionName, MAX_NETWORK_NODES)) {
            if (row.getLatitude() == null || row.getLongitude() == null) {
                continue;
            }
            nodes.add(new RouteNetworkResponse.Node(
                    row.getSpotId(), row.getName(), row.getLatitude(), row.getLongitude(), nullToZero(row.getVisitCount())));
            plottableSpotIds.add(row.getSpotId());
        }

        List<RouteNetworkResponse.Edge> edges = new ArrayList<>();
        for (DashboardRepository.NetworkEdgeRow row :
                dashboardRepository.findNetworkEdges(current.start(), current.end(), regionName, MAX_NETWORK_EDGES)) {
            if (!plottableSpotIds.contains(row.getFromSpotId()) || !plottableSpotIds.contains(row.getToSpotId())) {
                continue;
            }
            edges.add(new RouteNetworkResponse.Edge(row.getFromSpotId(), row.getToSpotId(), nullToZero(row.getWeight())));
        }

        return new RouteNetworkResponse(nodes, edges);
    }

    // 프론트의 "전체 지역"은 지역 필터 없음(null)으로 취급. InsightService와 같은 규칙.
    private String normalizeRegion(String region) {
        if (region == null || region.isBlank() || region.equals("전체 지역")) return null;
        return region;
    }

    private double ratio(long numerator, long denominator) {
        return denominator == 0 ? 0.0 : (double) numerator / denominator;
    }

    private double percentChange(double current, double previous) {
        if (previous == 0) return current == 0 ? 0.0 : 100.0;
        return ((current - previous) / previous) * 100.0;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
