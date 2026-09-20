package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 보고서 목록(카드 리스트)용. 본문(content)은 무거우니까 목록엔 안 실어보내고,
// 상세 조회(ReportDetailResponse)에서만 내려준다.
@Getter
@AllArgsConstructor
public class ReportSummaryResponse {
    private Long reportId;
    private String title;
    private String type;
    private String period;
    private String region;
    private String status;    // "COMPLETED" / "IN_PROGRESS" (프론트에서 "완료"/"작성 중"으로 매핑)
    private String createdAt; // "yyyy-MM-dd'T'HH:mm:ss" 형태 문자열
    private Long productId;   // 상품 기획안 유형일 때만 값이 있음 (PDF 생성 시 프론트가 이 id로 상품 재조회)
}
