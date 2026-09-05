package com.tripping.backend.mypage.service;

// 뱃지 고정 카탈로그. 지금은 일부러 비워둠 - "추가 작성자"/"연속 출석"은 실제 기획 없이
// 임시로 넣었던 가짜 뱃지라 제거함. 조회/저장(GET·PUT /users/me/badges) 기능 자체는
// 그대로 살아있고, 실제 뱃지 종류/획득 조건이 정해지면 여기 enum 상수로 추가하면 됨
// (그러면 MyPageBadgeService가 알아서 유저별로 노출 여부를 관리해줌).
public enum BadgeCatalog {
    ;

    private final String label;
    private final String emoji;

    BadgeCatalog(String label, String emoji) {
        this.label = label;
        this.emoji = emoji;
    }

    public String label() {
        return label;
    }

    public String emoji() {
        return emoji;
    }
}
