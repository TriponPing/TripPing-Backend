# Trip Ping API 엔드포인트 설계안

화면(기능 목록) 기준으로 정리했고, 기존에 정한 규칙(`/places` 네이밍, `/trips/search` → `/routes/{routeId}/places` 순차 조회, 추천 4개는 단일 POST 응답으로 반환)을 그대로 따랐어.

---

## 1. 인증 / 공통

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/auth/signup` | 회원가입 |
| POST | `/auth/login` | 로그인 |
| PATCH | `/users/me/language` | 언어 설정 변경 |

---

## 2. 홈

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/trips/ongoing` | 진행중인 여행 조회 |
| GET | `/trips/popular?period=week` | 이번주 인기 여행 조회 |
| GET | `/places/trending` | 떠오르는 인기 장소 조회 |
| GET | `/keywords/popular` | 인기 키워드 조회 |
| GET | `/trips/nearby?lat={lat}&lng={lng}` | 내 주변 여행 조회 |

---

## 3. 탐색 / 지도

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/places/search?lat={lat}&lng={lng}&radius={r}` | 지도 기반 검색 |
| GET | `/places?category=attraction` | 관광지 조회 |
| GET | `/places?category=restaurant` | 맛집 조회 |
| GET | `/places?category=cafe` | 카페 조회 |
| GET | `/places?category={cat}&filters={f}` | 탐색 지도 필터 기능 (카테고리·필터 파라미터 조합) |
| GET | `/places/{placeId}` | 장소 상세 조회 (장소 조회 겸용) |
| POST | `/places/{placeId}/bookmark` | 장소 북마크 저장 |
| DELETE | `/places/{placeId}/bookmark` | 장소 북마크 삭제 |

> "관광지/맛집/카페 조회"는 `category` 쿼리 파라미터로 하나의 `/places` 엔드포인트에 통합하는 걸 제안해. 엔드포인트가 3개로 늘어나는 것보다 유지보수가 쉬워.

---

## 4. 루트 계획 / 저장

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/regions/search?keyword={kw}` | 여행 지역 검색 |
| POST | `/routes/recommendations` | 맞춤 여행 루트 추천 (AI 4개 옵션을 한 응답에 반환, 탭 전환은 클라이언트에서 처리) |
| GET | `/routes/{routeId}` | 루트 상세 조회 |
| GET | `/routes/{routeId}/map` | 지도 불러오기 (저장된 장소 포함) |
| POST | `/routes/{routeId}/places` | 장소 추가 |
| PATCH | `/routes/{routeId}/places/{placeId}` | 장소 순서 · 내용 수정 |
| DELETE | `/routes/{routeId}/places/{placeId}` | 장소 삭제 |
| POST | `/trips` | 실제 여행으로 저장 |
| PATCH | `/trips/{tripId}/route` | 실제 루트로 저장 |
| GET | `/trips/search?name={name}` | 여행 이름으로 검색 |

> "추천 탭(4개) 조회"는 별도 GET이 아니라 `POST /routes/recommendations` 응답 안에 4개 옵션이 배열로 들어있고, 탭 전환은 클라이언트 상태값으로만 처리하는 구조로 뒀어 (기존에 합의한 방식).

---

## 5. Ping (여행 중 기록)

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/trips/{tripId}/pings` | 여행 Ping 기록 조회 (진행 중 여행 조회는 §2와 동일 엔드포인트 재사용) |
| POST | `/trips/{tripId}/pings` | 방문 장소 Ping 등록 |
| POST | `/pings/{pingId}/review` | Ping 후기 등록 |
| PATCH | `/pings/{pingId}/review` | Ping 후기 수정 |
| DELETE | `/pings/{pingId}/review` | Ping 후기 삭제 |

---

## 6. 커뮤니티

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/regions/{regionId}/routes` | 지역별 루트 조회 |
| GET | `/routes/{routeId}/reviews` | 루트 후기 조회 |
| PATCH | `/reviews/{reviewId}` | 댓글 수정 (작성자 본인만, 서버에서 소유자 검증) |
| DELETE | `/reviews/{reviewId}` | 댓글 삭제 (작성자 본인만, 서버에서 소유자 검증) |

---

## 7. 마이페이지

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/users/me` | 프로필 조회 |
| PATCH | `/users/me` | 프로필 수정 |
| GET | `/users/me/trips/recent` | 다녀온 여행 목록 조회 (요약) |
| GET | `/users/me/trips` | 다녀온 여행 전체 목록 조회 |
| GET | `/users/me/trips/{tripId}` | 여행 기록 상세 조회 |
| GET | `/users/me/routes/saved` | 저장한 루트 목록 조회 |
| DELETE | `/routes/{routeId}/saved` | 루트 저장 취소 (북마크 해제) |
| GET | `/users/me/map` | 나의 여행 지도 조회 (마이페이지용 미니 지도, 마커 좌표 요약) |
| GET | `/users/me/map/detail?type=drawn\|saved` | 나의 여행 지도 상세 조회 (자세히보기 화면. `type` 미지정 시 전체, `drawn`=내가 그린 루트만, `saved`=내가 저장한 루트만 — 화면의 "내가 그린 루트 보기"/"내가 저장한 루트 보기" 토글에 대응) |
| GET | `/users/me/map/search?name={kw}` | 지도 검색 (자세히보기 화면 상단 검색창, `/trips/search?name=`과 파라미터명 통일) |
| GET | `/routes/{routeId}` | 지도 마커 클릭 시 코스 미리보기 카드 (§4 기존 엔드포인트 재사용 — 별도 지도 상세 엔드포인트 불필요) |

---

## 8. B2B (대시보드 / 관광상품 기획)

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/b2b/dashboard` | 대시보드 (지역별 여행 트렌드 집계 — `ROUTE_CONDITION` GROUP BY 기반) |
| GET | `/b2b/products` | 관광상품 기획 목록 조회 |
| POST | `/b2b/products` | 관광상품 기획 생성 |
| GET | `/b2b/products/{productId}` | 관광상품 기획 상세 조회 |
| PATCH | `/b2b/products/{productId}` | 관광상품 기획 수정 |

---

## 설계 시 확인이 필요한 부분

1. **"진행중인 여행 조회"가 §2와 §5에 중복 등장** — 홈 화면용 요약과 Ping 기록 화면용 상세를 같은 엔드포인트로 쓸지, 응답 필드만 다르게 줄지 결정 필요.
2. **"장소 조회"(§3)와 "장소 상세 조회"(§3)가 별개 항목으로 나열됨** — 리스트용 `GET /places`와 단건용 `GET /places/{placeId}`로 이미 분리되어 있다고 가정했는데, 목록 화면에 카드 정보만 필요한지 확인 필요.
3. **"여행 기록 상세 조회"(§7)와 "루트 상세 조회"(§4)의 관계** — 여행(trip)과 루트(route)가 1:1인지, 여행 하나에 루트 변경 이력이 남는 구조인지에 따라 엔드포인트를 합칠 수도 있음.
