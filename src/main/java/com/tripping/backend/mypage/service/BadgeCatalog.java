package com.tripping.backend.mypage.service;

// 뱃지 고정 카탈로그. TODO: 실제 뱃지 획득 조건/종류가 정해지면 DB 테이블로 옮기고
// 획득 로직(여행 작성 횟수, 연속 출석일 등)을 여기 대신 별도 서비스에서 계산하도록 교체.
// 지금은 모든 유저가 이 카탈로그 전부를 "획득한" 상태로 취급하고, 그중 뭘 프로필에
// 노출할지(featured)만 UserBadgeSetting으로 관리함.
public enum BadgeCatalog {
    AUTHOR("추가 작성자", "📷"),
    ATTENDANCE("연속 출석", "📅");

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
