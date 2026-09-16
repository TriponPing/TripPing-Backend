package com.tripping.backend.home.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripDetailResponse {
    private Long routeId;
    // 루트 자체에 제목을 저장하는 필드가 없어서(ActualRoute에 title 컬럼 없음),
    // TouristSpotService.determineTheme()과 같은 방식으로 방문 장소 카테고리 비율을 보고
    // "역사탐방 루트" 같은 테마 이름을 즉석에서 계산해서 채움
    private String title;
    private String writerNickname;
    private String writerProfileImage;
    private String writerLevel;
    private List<StopSummaryResponse> stops;
    private int placeCount;
    private String photoUrl;
    private long savedCount;
    private List<CoordinateResponse> coordinates;
}