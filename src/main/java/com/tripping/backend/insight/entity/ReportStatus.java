package com.tripping.backend.insight.entity;

// 콘텐츠 신고용 entity.ReportStatus(PENDING/KEPT/DELETED, 게시물 신고 처리 상태)와는
// 완전히 다른 별개의 enum이다. 이름은 같지만 패키지가 달라서 안 헷갈리게 주석 남김 —
// 저건 "신고 처리 상태", 이건 "보고서 작성 상태".
public enum ReportStatus {
    COMPLETED,   // 완료 — 트렌드 데이터 기반으로 즉시 생성되는 보고서는 항상 이 상태로 저장됨
    IN_PROGRESS  // 작성 중 — 사람이 직접 채워야 하는 "상품 기획안" 등을 대비해 값만 만들어둠
                 // (지금 버전은 자동으로 이 상태가 되는 로직은 없음, 나중에 필요해지면 사용)
}
