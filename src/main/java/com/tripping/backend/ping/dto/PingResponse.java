package com.tripping.backend.ping.dto;

import com.tripping.backend.entity.WidgetPing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 핑(WIDGET_PING) 1건 응답
 */
public record PingResponse(
        Long pingId,
        Long spotId,
        String placeName,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime pingTime,
        Boolean isConfirmed
) {
    public static PingResponse from(WidgetPing ping) {
        return new PingResponse(
                ping.getWidgetPingId(),
                ping.getSpotId(),
                ping.getPlaceName(),
                ping.getLatitude(),
                ping.getLongitude(),
                ping.getPingTime(),
                ping.getIsConfirmed()
        );
    }
}
