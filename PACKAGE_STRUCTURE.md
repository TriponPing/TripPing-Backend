# 패키지 구조 ↔ API 스펙 매핑

`tripping-api-spec.md`의 섹션과 패키지가 1:1로 대응됩니다.

| 스펙 섹션 | 패키지 |
|---|---|
| 1. 인증 / 공통 | `auth` |
| 2. 홈 | `home` |
| 3. 탐색 / 지도 | `place` |
| 4. 루트 계획 / 저장 | `route`, `trip` |
| 5. Ping | `ping` |
| 6. 커뮤니티 | `community` |
| 7. 마이페이지 | `mypage` |
| 8. B2B | `b2b` |
| 공통 설정 · 예외 · 응답 포맷 | `global` |

각 도메인 패키지 내부는 4개 하위 폴더로 통일합니다:
```
domain/
├── controller/   # @RestController, @Tag(name="화면명")
├── service/      # 비즈니스 로직
├── repository/   # JPA Repository
└── dto/          # Request/Response DTO
```

담당자를 배정할 때도 이 표 기준으로 "나는 `route` 패키지 담당"처럼 나누면 스펙 문서 ↔ 코드 ↔ 이슈 라벨이 전부 같은 이름으로 맞아떨어집니다.
