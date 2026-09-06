package com.tripping.backend.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "위도/경도 좌표 한 점")
public class CoordinateResponse {

    private double latitude;
    private double longitude;
}
