package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.OrgApiKey;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 목록에서는 키 원문을 알 수 없다. 앞 8자만 보여준다.
// 발급 직후 한 번만 plainKey가 채워져 내려간다.
@Getter
@Builder
public class ApiKeyResponse {
    private Long keyId;
    private String label;
    private String keyPrefix;
    private String plainKey;
    private LocalDateTime createdAt;

    public static ApiKeyResponse from(OrgApiKey key) {
        return of(key, null);
    }

    public static ApiKeyResponse of(OrgApiKey key, String plainKey) {
        return ApiKeyResponse.builder()
                .keyId(key.getKeyId())
                .label(key.getLabel())
                .keyPrefix(key.getKeyPrefix())
                .plainKey(plainKey)
                .createdAt(key.getCreatedAt())
                .build();
    }
}
