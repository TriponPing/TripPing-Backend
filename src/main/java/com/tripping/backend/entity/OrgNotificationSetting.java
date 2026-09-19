package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 기관별 알림 수신 설정. 기관 하나당 한 행만 둔다(org_id 유니크).
// 행이 없으면 서비스에서 기본값으로 만들어 준다.
@Entity
@Table(name = "org_notification_setting")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrgNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @Column(name = "org_id", nullable = false, unique = true)
    private Long orgId;

    // 담당 지역에서 급상승 루트가 발견되면 알림
    @Builder.Default
    @Column(name = "notify_trend", nullable = false)
    private Boolean notifyTrend = true;

    // 보고서 생성이 끝나면 알림
    @Builder.Default
    @Column(name = "notify_report", nullable = false)
    private Boolean notifyReport = true;

    // 매주 지난주 방문 추이 메일
    @Builder.Default
    @Column(name = "notify_weekly", nullable = false)
    private Boolean notifyWeekly = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }
}
