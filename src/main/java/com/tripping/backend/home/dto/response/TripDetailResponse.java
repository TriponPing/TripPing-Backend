package com.tripping.backend.home.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripDetailResponse {
    private Long routeId;
    private String writerNickname;
    private String writerProfileImage;
    private String writerLevel;
    private List<StopSummaryResponse> stops;
    private int placeCount;
    private String photoUrl;
    private long savedCount;
    private List<CoordinateResponse> coordinates;
}