package com.tripping.backend.ping.dto;

public record SpotPingStatsResponse(
        String popularTimeSlot, // 가장 많이 가는 시간대 (예: "오후")
        long totalPingCount     // 총 핑 개수 (예: 2)
) {}