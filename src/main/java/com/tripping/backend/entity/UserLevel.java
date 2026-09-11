package com.tripping.backend.entity;

/**
 * "Pinger(핑거)" 레벨 시스템 - 지금까지 찍은 핑(ACTUAL_ROUTE_SPOT) 누적 개수로 자동 결정됨.
 * AppUser에 별도로 저장하지 않고, 프로필을 조회할 때마다 그 시점의 총 핑 개수를 기준으로
 * fromPingCount()가 매번 새로 계산함 - 핑을 찍자마자 레벨업, 기록을 지우면 레벨다운까지
 * 항상 실제 활동량과 정확히 일치함(별도 갱신 로직/버그 걱정 없음).
 */
public enum UserLevel {
    새싹핑거(0),
    아기핑거(10),
    꼬마핑거(20),
    약지핑거(40),
    중지핑거(60),
    검지핑거(85),
    척척핑거(120),
    엄지핑거(160),
    프로핑거(210),
    마스터핑거(250);

    private final int requiredPingCount;

    UserLevel(int requiredPingCount) {
        this.requiredPingCount = requiredPingCount;
    }

    public int getRequiredPingCount() {
        return requiredPingCount;
    }

    // 총 핑 개수에 맞는 가장 높은 레벨을 찾음 (등급을 낮은 것부터 훑어서 조건을 만족하는 마지막 등급)
    public static UserLevel fromPingCount(int totalPingCount) {
        UserLevel result = 새싹핑거;
        for (UserLevel level : values()) {
            if (totalPingCount >= level.requiredPingCount) {
                result = level;
            } else {
                break;
            }
        }
        return result;
    }
}
