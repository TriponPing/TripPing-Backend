package com.tripping.backend.insight.service;

import com.tripping.backend.b2b.repository.OrganizationRepository;
import com.tripping.backend.b2b.repository.TourProductRepository;
import com.tripping.backend.community.repository.RegionRepository;
import com.tripping.backend.entity.Organization;
import com.tripping.backend.entity.Region;
import com.tripping.backend.entity.TourProduct;
import com.tripping.backend.insight.dto.ReportCreateRequest;
import com.tripping.backend.insight.dto.ReportDetailResponse;
import com.tripping.backend.insight.dto.ReportSummaryResponse;
import com.tripping.backend.insight.dto.RouteRankingResponse;
import com.tripping.backend.insight.dto.TrendsSummaryResponse;
import com.tripping.backend.insight.entity.InsightReport;
import com.tripping.backend.insight.entity.ReportStatus;
import com.tripping.backend.insight.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class ReportService {

    private static final String ALL_REGIONS_LABEL = "전체 지역";
    private static final Map<Integer, String> DURATION_LABELS =
            Map.of(480, "당일", 1440, "1박 2일", 2880, "2박 3일");

    private final ReportRepository reportRepository;
    private final OrganizationRepository organizationRepository;
    private final TourProductRepository productRepository;
    private final RegionRepository regionRepository;
    private final InsightService insightService; // reuse trend calculation logic (summary + rising routes)

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Generate a report. "상품 기획안" reports are snapshotted from a specific tour_product;
    // every other type (트렌드 분석/루트 네트워크) is snapshotted from the same trend calculation
    // the Trends screen uses, frozen into text at creation time.
    public ReportDetailResponse create(Long orgId, ReportCreateRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found."));

        if (request.getProductId() != null) {
            return createFromProduct(org, request);
        }

        // 보고서는 "만든 시점" 기준 스냅샷이라 항상 오늘 기준으로 계산한다 (트렌드 화면의
        // 날짜 선택기와 달리 과거 기준일을 지정할 방법이 없음).
        DateRange range = DateRange.forPeriod(request.getPeriod(), LocalDate.now());
        TrendsSummaryResponse summary = insightService.summary(request.getRegion(), range);
        List<RouteRankingResponse> routes = insightService.risingRoutes(request.getRegion(), range);

        InsightReport report = InsightReport.builder()
                .organization(org)
                .title(request.getTitle())
                .type(request.getType())
                .period(request.getPeriod())
                .region(request.getRegion())
                .content(buildContent(request, summary, routes))
                .status(ReportStatus.COMPLETED)
                .build();

        reportRepository.save(report);
        return toDetail(report);
    }

    // 상품 기획안 보고서는 트렌드 수치가 아니라 특정 tour_product 스냅샷이다. period/region
    // 컬럼은 그대로 두되(스키마 변경을 최소화하려고), 목록 카드에 트렌드 보고서와 비슷하게
    // "여행 기간 · 지역"이 보이도록 그 의미만 상품 쪽 값으로 바꿔서 채운다.
    private ReportDetailResponse createFromProduct(Organization org, ReportCreateRequest request) {
        TourProduct product = productRepository
                .findByProductIdAndOrgIdAndIsDeletedFalse(request.getProductId(), org.getOrgId())
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        InsightReport report = InsightReport.builder()
                .organization(org)
                .title(request.getTitle())
                .type(request.getType())
                .period(durationLabel(product.getExpectedDuration()))
                .region(regionName(product.getRegionId()))
                .productId(product.getProductId())
                .content(buildProductContent(product))
                .status(ReportStatus.COMPLETED)
                .build();

        reportRepository.save(report);
        return toDetail(report);
    }

    // Report list (card list).
    public List<ReportSummaryResponse> list(Long orgId) {
        return reportRepository.findByOrganization_OrgIdOrderByCreatedAtDesc(orgId).stream()
                .map(this::toSummary)
                .toList();
    }

    // Report detail (for viewing/downloading, includes content).
    public ReportDetailResponse get(Long orgId, Long reportId) {
        InsightReport report = reportRepository.findByReportIdAndOrganization_OrgId(reportId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found."));
        return toDetail(report);
    }

    // Delete a report. Reuses the same ownership-checked lookup as get() so a report ID from
    // another organization can't be deleted just by guessing the URL.
    public void delete(Long orgId, Long reportId) {
        InsightReport report = reportRepository.findByReportIdAndOrganization_OrgId(reportId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found."));
        reportRepository.delete(report);
    }

    // Assemble the trends API result into a human-readable report body.
    // Matches the shape of the frontend's buildReportText() so the frontend can render this
    // content directly on screen or in a PDF later without it looking out of place.
    private String buildContent(ReportCreateRequest request, TrendsSummaryResponse summary, List<RouteRankingResponse> routes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trip Ping Insight Report\n");
        sb.append("Period: ").append(request.getPeriod()).append("\n");
        sb.append("Region: ").append(request.getRegion()).append("\n\n");
        sb.append("Total visit pings: ").append(summary.getTotalVisits())
                .append(" (").append(formatRate(summary.getChangeRate())).append("% vs previous period)\n");

        if (routes.isEmpty()) {
            // No confirmed routes in this period/region (new organization, off-season, etc.)
            sb.append("Rising route: no routes recorded in this period.");
        } else {
            RouteRankingResponse top = routes.get(0); // already sorted by visit count desc (see InsightService)
            sb.append("Top route: ").append(top.getRouteName()).append("\n");
            sb.append("Rising route: ").append(top.getRouteName())
                    .append(" (").append(formatRate(top.getChangeRate())).append("%)");
        }
        return sb.toString();
    }

    // Prefixes a "+" for non-negative rates (Java does not add "+" to positive numbers automatically).
    private String formatRate(double rate) {
        return (rate >= 0 ? "+" : "") + rate;
    }

    // 상품 개요를 사람이 읽을 문장으로 굳힌다. buildContent()와 같은 발상이지만 트렌드 수치
    // 대신 상품 필드(이름/상태/가격/기간/대상)를 쓴다.
    private String buildProductContent(TourProduct product) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trip Ping Product Plan\n");
        sb.append("Product: ").append(product.getProductName()).append("\n");
        sb.append("Status: ").append(product.getStatus()).append("\n");
        sb.append("Region: ").append(regionName(product.getRegionId())).append("\n");
        sb.append("Duration: ").append(durationLabel(product.getExpectedDuration())).append("\n");
        if (product.getTargetCustomer() != null) {
            sb.append("Target: ").append(product.getTargetCustomer()).append("\n");
        }
        if (product.getPrice() != null) {
            sb.append("Price: ").append(product.getPrice()).append("\n");
        }
        if (product.getDescription() != null) {
            sb.append("\n").append(product.getDescription());
        }
        return sb.toString();
    }

    private String regionName(String regionId) {
        if (regionId == null || regionId.isBlank()) return ALL_REGIONS_LABEL;
        return regionRepository.findById(regionId).map(Region::getRegionName).orElse(ALL_REGIONS_LABEL);
    }

    // 프론트 lib/productMapping.ts의 DURATION_MINUTES와 반드시 같은 값을 써야 한다 — 상품
    // 화면에서 고르는 "당일/1박 2일/2박 3일"이 분 단위로 저장되는 값 그대로.
    private String durationLabel(Integer minutes) {
        if (minutes == null) return "당일";
        return DURATION_LABELS.getOrDefault(minutes, "당일");
    }

    private ReportSummaryResponse toSummary(InsightReport r) {
        return new ReportSummaryResponse(r.getReportId(), r.getTitle(), r.getType(), r.getPeriod(),
                r.getRegion(), r.getStatus().name(), r.getCreatedAt().format(TIMESTAMP_FORMAT), r.getProductId());
    }

    private ReportDetailResponse toDetail(InsightReport r) {
        return new ReportDetailResponse(r.getReportId(), r.getTitle(), r.getType(), r.getPeriod(),
                r.getRegion(), r.getStatus().name(), r.getCreatedAt().format(TIMESTAMP_FORMAT), r.getContent(),
                r.getProductId());
    }
}
