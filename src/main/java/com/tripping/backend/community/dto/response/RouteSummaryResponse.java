package com.tripping.backend.community.dto.response;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "지역별 루트 목록 응답")
public class RouteSummaryResponse {

    @Schema(description = "루트 ID", example = "1")
    private Long routeId;

    @Schema(description = "작성자 닉네임", example = "여행좋아")
    private String writerNickname;

    @Schema(description = "여행 날짜", example = "2026-08-20")
    private LocalDate travelDate;

    @Schema(description = "동행 유형", example = "가족")
    private String companionType;

    @Schema(description = "이동 수단", example = "대중교통")
    private String transport;

    @Schema(description = "인원 수", example = "3")
    private Integer memberCount;

    @Schema(description = "루트 진행 상태")
    private RouteStatus status;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    /**
     * ActualRoute 엔티티는 작성자 닉네임을 갖고 있지 않아(user_id만 있음)
     * 별도로 조회한 닉네임을 파라미터로 받습니다.
     */
    public static RouteSummaryResponse from(ActualRoute route, String writerNickname) {
        return RouteSummaryResponse.builder()
                .routeId(route.getActualRouteId())
                .writerNickname(writerNickname)
                .travelDate(route.getTravelDate())
                .companionType(route.getCompanionType())
                .transport(route.getTransport())
                .memberCount(route.getMemberCount())
                .status(route.getStatus())
                .createdAt(route.getCreatedAt())
                .build();
    }
}
