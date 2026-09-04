package com.tripping.backend.home.dto.response;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이번주 인기 여행 응답")
public class PopularTripResponse {

    @Schema(description = "루트 ID", example = "1")
    private Long routeId;

    @Schema(description = "작성자 닉네임", example = "여행좋아")
    private String writerNickname;

    @Schema(description = "여행 날짜")
    private LocalDate travelDate;

    @Schema(description = "동행 유형")
    private String companionType;

    @Schema(description = "이동 수단")
    private String transport;

    @Schema(description = "인원 수")
    private Integer memberCount;

    @Schema(description = "루트 진행 상태")
    private RouteStatus status;

    @Schema(description = "이번 주 저장(찜) 수", example = "12")
    private Long savedCount;

    public static PopularTripResponse from(ActualRoute route, String writerNickname, long savedCount) {
        return PopularTripResponse.builder()
                .routeId(route.getActualRouteId())
                .writerNickname(writerNickname)
                .travelDate(route.getTravelDate())
                .companionType(route.getCompanionType())
                .transport(route.getTransport())
                .memberCount(route.getMemberCount())
                .status(route.getStatus())
                .savedCount(savedCount)
                .build();
    }
}
