package com.tripping.backend.insight.service;

import com.tripping.backend.b2b.repository.OrganizationRepository;
import com.tripping.backend.entity.Organization;
import com.tripping.backend.insight.dto.ReportCreateRequest;
import com.tripping.backend.insight.dto.ReportDetailResponse;
import com.tripping.backend.insight.dto.ReportSummaryResponse;
import com.tripping.backend.insight.dto.RouteRankingResponse;
import com.tripping.backend.insight.dto.TrendsSummaryResponse;
import com.tripping.backend.insight.entity.Report;
import com.tripping.backend.insight.entity.ReportStatus;
import com.tripping.backend.insight.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final OrganizationRepository organizationRepository;
    private final InsightService insightService; // 트렌드 계산 로직(요약 + 급상승 루트)을 그대로 재사용

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // 보고서 생성: 별도로 쌓아둔 통계 데이터가 있는 게 아니라, 트렌드 API가 쓰는 것과
    // 완전히 똑같은 InsightService.summary()/risingRoutes()를 그 자리에서 호출해서
    // 결과를 텍스트로 굳힌 다음 저장한다. (프론트 dashboardData.ts의 buildReportText()와
    // 같은 발상 — 저건 목데이터로, 이건 진짜 DB 집계 결과로 만드는 버전)
    public ReportDetailResponse create(Long orgId, ReportCreateRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기관입니다."));

        TrendsSummaryResponse summary = insightService.summary(request.getPeriod(), request.getRegion());
        List<RouteRankingResponse> routes = insightService.risingRoutes(request.getPeriod(), request.getRegion());

        Report report = Report.builder()
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

    // 보고서 목록 (카드 리스트)
    public List<ReportSummaryResponse> list(Long orgId) {
        return reportRepository.findByOrganization_OrgIdOrderByCreatedAtDesc(orgId).stream()
                .map(this::toSummary)
                .toList();
    }

    // 보고서 상세 (다운로드/열람용, content 포함)
    public ReportDetailResponse get(Long orgId, Long reportId) {
        Report report = reportRepository.findByReportIdAndOrganization_OrgId(reportId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("보고서를 찾을 수 없습니다."));
        return toDetail(report);
    }

    // 트렌드 API 결과를 사람이 읽는 보고서 문장으로 조립.
    // 프론트 buildReportText()와 형식을 맞춰서, 나중에 프론트가 이 content를 그대로
    // 화면/PDF에 뿌려도 어색하지 않게 만들었다.
    private String buildContent(ReportCreateRequest request, TrendsSummaryResponse summary, List<RouteRankingResponse> routes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trip Ping Insight Report\n");
        sb.append("기간: ").append(request.getPeriod()).append("\n");
        sb.append("지역: ").append(request.getRegion()).append("\n\n");
        sb.append("방문 핑: ").append(summary.getTotalVisits())
                .append(" (전 기간 대비 ").append(formatRate(summary.getChangeRate())).append("%)\n");

        if (routes.isEmpty()) {
            // 해당 기간·지역에 방문 확정된 루트가 하나도 없는 경우 (신규 기관, 비수기 등)
            sb.append("급상승 루트: 해당 기간에 집계된 루트가 없습니다.");
        } else {
            RouteRankingResponse top = routes.get(0); // 이미 방문 수 내림차순 정렬되어 있음 (InsightService 참고)
            sb.append("인기 루트: ").append(top.getRouteName()).append("\n");
            sb.append("급상승 루트: ").append(top.getRouteName())
                    .append(" (").append(formatRate(top.getChangeRate())).append("%)");
        }
        return sb.toString();
    }

    // 증감률 앞에 부호를 붙여준다 (+24.8 / -12.3). 0 이상이면 +를 직접 붙여야 함
    // (Java는 양수에 자동으로 +를 안 붙여줌).
    private String formatRate(double rate) {
        return (rate >= 0 ? "+" : "") + rate;
    }

    private ReportSummaryResponse toSummary(Report r) {
        return new ReportSummaryResponse(r.getReportId(), r.getTitle(), r.getType(), r.getPeriod(),
                r.getRegion(), r.getStatus().name(), r.getCreatedAt().format(TIMESTAMP_FORMAT));
    }

    private ReportDetailResponse toDetail(Report r) {
        return new ReportDetailResponse(r.getReportId(), r.getTitle(), r.getType(), r.getPeriod(),
                r.getRegion(), r.getStatus().name(), r.getCreatedAt().format(TIMESTAMP_FORMAT), r.getContent());
    }
}
