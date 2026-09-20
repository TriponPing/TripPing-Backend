package com.tripping.backend.insight.controller;

import com.tripping.backend.entity.Organization;
import com.tripping.backend.insight.dto.ReportCreateRequest;
import com.tripping.backend.insight.dto.ReportDetailResponse;
import com.tripping.backend.insight.dto.ReportSummaryResponse;
import com.tripping.backend.insight.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// "보고서 센터"(TripPing-Web pages/Reports.tsx) 전용 API.
// 트렌드(InsightController)는 항상 실시간 재계산이지만, 여기는 생성 시점 결과를 DB에
// 저장해서 목록으로 다시 볼 수 있게 한다 — 그래서 로직도 별도 컨트롤러/서비스로 분리함.
// 로그인 안 한 사람은 SecurityConfig의 anyRequest().authenticated()에 걸려서 여기 오기 전에 막힘.
@Tag(name = "B2B 인사이트 - 보고서", description = "트렌드 데이터를 기반으로 생성·저장하는 보고서 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/insight/reports")
public class ReportController {

    private final ReportService reportService;

    // [보고서 생성] POST /b2b/insight/reports
    // body: { title, type, period, region }
    @Operation(summary = "보고서 생성", description = "트렌드 API와 동일한 로직으로 즉시 계산한 뒤 결과를 저장한다.")
    @PostMapping
    public ResponseEntity<ReportDetailResponse> create(@RequestBody ReportCreateRequest request,
                                                         Authentication authentication) {
        Long orgId = currentOrgId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(orgId, request));
    }

    // [보고서 목록] GET /b2b/insight/reports — 로그인한 기관이 만든 것만 최신순으로
    @Operation(summary = "내 기관이 만든 보고서 목록 조회")
    @GetMapping
    public ResponseEntity<List<ReportSummaryResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(reportService.list(currentOrgId(authentication)));
    }

    // [보고서 상세/다운로드] GET /b2b/insight/reports/{reportId}
    @Operation(summary = "보고서 상세 조회 (다운로드용 본문 포함)")
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDetailResponse> get(@PathVariable Long reportId, Authentication authentication) {
        return ResponseEntity.ok(reportService.get(currentOrgId(authentication), reportId));
    }

    // [보고서 삭제] DELETE /b2b/insight/reports/{reportId}
    @Operation(summary = "보고서 삭제")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> delete(@PathVariable Long reportId, Authentication authentication) {
        reportService.delete(currentOrgId(authentication), reportId);
        return ResponseEntity.noContent().build();
    }

    // 로그인 주체가 기관(Organization)인지 확인하고 orgId를 꺼낸다.
    // Admin(운영자) 계정으로는 보고서를 만들 수 없게 막아둠 — 보고서는 기관 담당자 전용 기능이라서
    // (B2bAuthService.me()에서 principal이 Admin/Organization 중 뭔지 구분하는 것과 같은 패턴).
    private Long currentOrgId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Organization org) {
            return org.getOrgId();
        }
        throw new IllegalStateException("기관 계정으로 로그인해야 보고서를 사용할 수 있습니다.");
    }
}
