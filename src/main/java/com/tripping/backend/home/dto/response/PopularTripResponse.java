package com.tripping.backend.home.dto.response;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
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

    @Schema(description = "방문 순서대로의 스팟 이름 목록", example = "[\"성수\", \"서울 숲\", \"한강\"]")
    private List<String> stopNames;

    @Schema(description = "대표 사진 URL (첫 방문 스팟의 관광지 사진, 없으면 null)")
    private String photoUrl;

    @Schema(description = "총 Ping(방문 스팟) 개수")
    private long pingCount;

    @Schema(description = "실제 GPS 핑이 찍힌 방문 순서대로의 좌표 목록 (핑이 없는 스팟은 제외, 지도/경로 표시용)")
    private List<CoordinateResponse> coordinates;

    public static PopularTripResponse from(
            ActualRoute route,
            String writerNickname,
            long savedCount,
            List<String> stopNames,
            String photoUrl,
            long pingCount,
            List<CoordinateResponse> coordinates
    ) {
        return PopularTripResponse.builder()
                .routeId(route.getActualRouteId())
                .writerNickname(writerNickname)
                .travelDate(route.getTravelDate())
                .companionType(route.getCompanionType())
                .transport(route.getTransport())
                .memberCount(route.getMemberCount())
                .status(route.getStatus())
                .savedCount(savedCount)
                .stopNames(stopNames)
                .photoUrl(photoUrl)
                .pingCount(pingCount)
                .coordinates(coordinates)
                .build();
    }
}
