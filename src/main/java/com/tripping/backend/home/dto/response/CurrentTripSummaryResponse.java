package com.tripping.backend.home.dto.response;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 홈 화면 "여행 중" 카드 전용 응답.
 * ActualRoute(진행 중 여행) 정보 + WidgetPing 실데이터(개수, 방문 장소 이름)를 한 번에 내려줌.
 * Ping 도메인 코드/엔드포인트는 건드리지 않고, WidgetPing 엔티티만 홈 도메인에서 별도 조회함.
 */
@Getter
@Builder
@Schema(description = "홈 화면 진행 중인 여행 요약 응답")
public class CurrentTripSummaryResponse {

    @Schema(description = "루트 ID", example = "12")
    private Long actualRouteId;

    @Schema(description = "동행 유형", example = "FRIEND")
    private String companionType;

    @Schema(description = "이동 수단", example = "WALK")
    private String transport;

    @Schema(description = "인원 수", example = "2")
    private Integer memberCount;

    @Schema(description = "여행 날짜")
    private LocalDate travelDate;

    @Schema(description = "루트 진행 상태")
    private RouteStatus status;

    @Schema(description = "지금까지 등록된 Ping 개수", example = "3")
    private long pingCount;

    @Schema(description = "지금까지 등록된 Ping의 방문 장소 이름 목록 (등록 순서)")
    private List<String> visitedPlaceNames;

    public static CurrentTripSummaryResponse of(ActualRoute route, long pingCount, List<String> visitedPlaceNames) {
        return CurrentTripSummaryResponse.builder()
                .actualRouteId(route.getActualRouteId())
                .companionType(route.getCompanionType())
                .transport(route.getTransport())
                .memberCount(route.getMemberCount())
                .travelDate(route.getTravelDate())
                .status(route.getStatus())
                .pingCount(pingCount)
                .visitedPlaceNames(visitedPlaceNames)
                .build();
    }
}
