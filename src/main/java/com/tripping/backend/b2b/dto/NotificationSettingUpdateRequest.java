package com.tripping.backend.b2b.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationSettingUpdateRequest {
    private Boolean notifyTrend;
    private Boolean notifyReport;
}
