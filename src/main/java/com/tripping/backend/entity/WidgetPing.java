package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "widget_ping")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WidgetPing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "widget_ping_id")
    private Long widgetPingId;

    @Column(name = "actual_route_id", nullable = false)
    private Long actualRouteId;

    @Column(name = "spot_id")
    private Long spotId; // 검색으로 매칭된 관광지, 직접 입력 시 NULL 가능

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName; // 실시간 입력한 방문지 이름

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "ping_time", nullable = false)
    private LocalDateTime pingTime;

    @Builder.Default
    @Column(name = "is_confirmed", nullable = false)
    private Boolean isConfirmed = false; // 여행 종료 시 최종 확정 여부

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 소프트 삭제 여부 - 핑 삭제

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        if (this.pingTime == null) this.pingTime = now;
    }
}