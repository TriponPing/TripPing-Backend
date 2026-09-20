package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 대시보드 "여행 루트 네트워크" 지도용. 실제 방문 기록에서 뽑은 노드(관광지)와
// 엣지(같은 여행에서 바로 이어서 방문한 관광지 쌍)를 그대로 내려준다.
// 좌표는 핑 시점 실제 GPS 평균(없으면 관광지 등록 좌표)이라 프론트에서 그대로 투영하면 된다.
@Getter
@AllArgsConstructor
public class RouteNetworkResponse {

    private List<Node> nodes;
    private List<Edge> edges;

    @Getter
    @AllArgsConstructor
    public static class Node {
        private Long spotId;
        private String name;
        private Double latitude;
        private Double longitude;
        private long visitCount;   // 이 관광지에 찍힌 방문 핑 수 (노드 크기용)
    }

    @Getter
    @AllArgsConstructor
    public static class Edge {
        private Long fromSpotId;
        private Long toSpotId;
        private long weight;       // 이 이동이 관측된 횟수 (선 굵기용)
    }
}
