package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.OrgStatus;
import lombok.Getter;

@Getter
public class OrgReviewRequestDto {
    private OrgStatus status; // "APPROVED" 또는 "REJECTED" 문자열로 보내면 자동 매핑됨
}
