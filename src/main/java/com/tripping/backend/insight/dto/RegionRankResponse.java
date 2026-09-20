package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 대시보드 "지역별 인기" 한 줄. 두 종류의 실데이터를 한 행에 같이 담는다.
//
//  - visitPings    : 우리 Trip Ping 자체 방문 핑 수 (아직 희소할 수 있음)
//  - regionVisitors: 한국관광공사 DataLab 기준 그 지역의 실제 방문자수 (조밀함)
//
// 랭킹 정렬 기준은 regionVisitors(관광공사)다 — 자체 데이터만으로는 대부분 0이라
// 순위가 의미를 갖지 못하기 때문. 자체 핑은 "우리가 이 지역을 얼마나 덮고 있는지"를
// 같이 보여주는 보조 지표로 표시한다.
@Getter
@AllArgsConstructor
public class RegionRankResponse {

    private String regionName;       // region 테이블의 region_name 그대로 (예: "제주", "경기")
    private long visitPings;         // Trip Ping 자체 방문 핑 수
    private Long regionVisitors;     // 관광공사 기준 기간 내 총 방문자수. 매핑/조회 실패 시 null
}
