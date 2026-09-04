package com.tripping.backend.mypage.dto;

import lombok.Getter;

/**
 * 루트 저장(북마크) / 저장 취소 응답
 * POST /routes/{routeId}/saved
 */
@Getter
public class SavedRouteSaveResponse {
    private Long actualRouteId;
    private boolean saved;

    public SavedRouteSaveResponse(Long actualRouteId, boolean saved) {
        this.actualRouteId = actualRouteId;
        this.saved = saved;
    }
}
