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

    // 👈 새로 추가: visitedPlaceNames 중 앞에서부터 몇 개가 실제로 confirm(visitTime 등록)된
    // 핑인지. 홈 위젯에서 이미 찍은 핑(파란색)과 다음에 찍을 핑(포커스)을 구분해서 그리기 위함.
    @Schema(description = "visitedPlaceNames 중 실제로 찍힌(visitTime 있는) 핑 개수 - 앞에서부터 이 개수만큼이 확정된 핑", example = "2")
    private long confirmedCount;

    // 👈 새로 추가: "내 주변 코스"를 마지막으로 찍은 핑 위치 기준으로 보여주기 위한 좌표.
    // 아직 찍은 핑이 하나도 없으면 둘 다 null.
    @Schema(description = "가장 최근에 찍힌 핑의 위도 (없으면 null)")
    private Double lastPingLatitude;

    @Schema(description = "가장 최근에 찍힌 핑의 경도 (없으면 null)")
    private Double lastPingLongitude;

    public static CurrentTripSummaryResponse of(
            ActualRoute route,
            long pingCount,
            List<String> visitedPlaceNames,
            long confirmedCount,
            Double lastPingLatitude,
            Double lastPingLongitude
    ) {
        return CurrentTripSummaryResponse.builder()
                .actualRouteId(route.getActualRouteId())
                .companionType(route.getCompanionType())
                .transport(route.getTransport())
                .memberCount(route.getMemberCount())
                .travelDate(route.getTravelDate())
                .status(route.getStatus())
                .pingCount(pingCount)
                .visitedPlaceNames(visitedPlaceNames)
                .confirmedCount(confirmedCount)
                .lastPingLatitude(lastPingLatitude)
                .lastPingLongitude(lastPingLongitude)
                .build();
    }
}
