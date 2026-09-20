package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 주어진 관광지들(spotIds)에 실제로 남겨진 Pinger 평점·후기를 그대로 집계해서 보여준다.
// "상품 기획안" 보고서가 관리자가 입력한 스펙이 아니라 실제 방문자 반응을 근거로 삼도록
// 하기 위한 API — ping_log.rating/review_comment를 새로 지어내지 않고 그대로 쓴다.
@Getter
@AllArgsConstructor
public class SpotEvidenceResponse {
    private Double averageRating;      // 평점 없으면 null (지어내지 않음)
    private long ratingCount;          // 평점이 달린 핑 개수
    private List<String> sampleComments; // 최신순 실제 후기 코멘트 (최대 3개, 빈 코멘트는 제외)
}
