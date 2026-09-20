package com.tripping.backend.insight.service;

import com.tripping.backend.b2b.repository.OrganizationRepository;
import com.tripping.backend.entity.Organization;
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

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final OrganizationRepository organizationRepository;
    private final InsightService insightService; // reuse trend calculation logic (summary + rising routes)

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Generate a report: not backed by separately-stored stats, instead calls the exact same
    // InsightService.summary()/risingRoutes() the trends API uses, right now, and freezes the
    // result into text before saving. (Same idea as the frontend's buildReportText(), but this
    // version pulls real aggregated numbers from the DB instead of mock data.)
    public ReportDetailResponse create(Long orgId, ReportCreateRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found."));

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

    private ReportSummaryResponse toSummary(InsightReport r) {
        return new ReportSummaryResponse(r.getReportId(), r.getTitle(), r.getType(), r.getPeriod(),
                r.getRegion(), r.getStatus().name(), r.getCreatedAt().format(TIMESTAMP_FORMAT));
    }

    private ReportDetailResponse toDetail(InsightReport r) {
        return new ReportDetailResponse(r.getReportId(), r.getTitle(), r.getType(), r.getPeriod(),
                r.getRegion(), r.getStatus().name(), r.getCreatedAt().format(TIMESTAMP_FORMAT), r.getContent());
    }
}
