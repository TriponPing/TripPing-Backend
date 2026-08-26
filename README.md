# TripPing-BE

여행 루트 기록 및 관광 트렌드 분석 플랫폼 — 공용 백엔드 (Android 앱 · React 웹 공용)

## Stack
- Java / Spring Boot
- Supabase PostgreSQL
- springdoc-openapi (Swagger)

## 패키지 구조
`PACKAGE_STRUCTURE.md` 참고 — API 스펙 문서의 화면 단위(§1~§8)와 패키지가 1:1로 매핑되어 있습니다.

## 실행 방법
```bash

```

## API 문서 (Swagger)
서버 실행 후 아래 주소에서 확인:
```
http://localhost:8080/swagger-ui.html
```

## 브랜치 전략
- `main` : 배포 브랜치, PR + 승인 없이 머지 불가
- `develop` : 통합 개발 브랜치, PR + 승인 없이 머지 불가
- `feat/기능명` : `develop`에서 분기, 작업 후 `develop`으로 PR
- `fix/버그명` : 버그 수정, 작업 후 `develop`으로 PR

## 이슈 / PR
`.github/ISSUE_TEMPLATE`, `.github/PULL_REQUEST_TEMPLATE.md` 참고.
이슈 라벨은 화면 단위(홈 / 탐색·지도 / 루트계획 / Ping / 커뮤니티 / 마이페이지 / B2B)로 통일합니다.
