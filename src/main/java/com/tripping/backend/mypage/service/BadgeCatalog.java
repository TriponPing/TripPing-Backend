package com.tripping.backend.mypage.service;

// 뱃지 고정 카탈로그 - "Pinger" 컨셉(핑거 = 손가락)에 맞춰 10종으로 구성.
// UserLevel(핑 누적 개수 기준 등급)과 겹치지 않도록, 단순 핑 개수가 아니라
// "어떤 행동을 했는지"(지역핑 등록, 다른 지역 탐방, 여행 완주, 후기/태그 작성, 저장 등)
// 다양한 활동 종류를 기준으로 잡음. 실제 달성 여부는 MyPageBadgeService가 매번 새로 계산함
// (UserLevel과 같은 이유 - 저장된 값이 아니라 실제 활동 데이터 기준으로 항상 정확하게).
public enum BadgeCatalog {

    FIRST_PING("첫 발걸음", "🐾", "첫 핑을 찍어보세요"),
    ROUTE_FINISHER("완주의 기쁨", "🏁", "여행 1개를 완주해보세요"),
    ROUTE_VETERAN("다작 여행가", "🧳", "여행 5개를 완주해보세요"),
    REGION_PING_STARTER("동네 탐험대장", "🏘️", "내 지역에 지역핑을 처음 등록해보세요"),
    REGION_EXPLORER("지역 정복자", "🗺️", "서로 다른 지역 5곳에서 핑을 찍어보세요"),
    NATIONWIDE_EXPLORER("전국일주 탐험가", "🧭", "서로 다른 지역 10곳에서 핑을 찍어보세요"),
    STORYTELLER("이야기꾼", "📝", "후기를 10개 남겨보세요"),
    TAG_COLLECTOR("태그 수집가", "🏷️", "태그를 30개 달아보세요"),
    TREASURE_KEEPER("보물 창고", "🎒", "장소나 루트를 10개 저장해보세요"),
    STAR_RATER("별빛 감별사", "⭐", "별점을 20번 남겨보세요");

    private final String label;
    private final String emoji;
    private final String conditionDesc;

    BadgeCatalog(String label, String emoji, String conditionDesc) {
        this.label = label;
        this.emoji = emoji;
        this.conditionDesc = conditionDesc;
    }

    public String label() {
        return label;
    }

    public String emoji() {
        return emoji;
    }

    public String conditionDesc() {
        return conditionDesc;
    }
}
