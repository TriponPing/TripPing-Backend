package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 보고서 상세/다운로드용. ReportSummaryResponse와 필드 다 같고 content(본문 텍스트)만 추가됨.
// 보고서 생성 직후 응답, 그리고 상세 조회 API 둘 다 이걸 씀.
@Getter
@AllArgsConstructor
public class ReportDetailResponse {
    private Long reportId;
    private String title;
    private String type;
    private String period;
    private String region;
    private String status;
    private String createdAt;
    private String content; // PDF로 내보낼 때 프론트에서 이 텍스트를 그대로 렌더링하면 됨
}
