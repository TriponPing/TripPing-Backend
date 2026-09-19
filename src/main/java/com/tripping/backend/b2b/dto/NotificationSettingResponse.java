package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.OrgNotificationSetting;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSettingResponse {
    private Boolean notifyTrend;
    private Boolean notifyReport;
    private Boolean notifyWeekly;

    public static NotificationSettingResponse from(OrgNotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .notifyTrend(setting.getNotifyTrend())
                .notifyReport(setting.getNotifyReport())
                .notifyWeekly(setting.getNotifyWeekly())
                .build();
    }
}
