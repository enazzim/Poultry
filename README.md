# PoultryShare — 양계 수급 공개 포털

하이브레인넷형 **업종 특화 공개 포털**입니다.  
농가·파트너가 OFFER/NEED 공고를 양방향으로 올리고, 관심·문의로 연결합니다. (계약은 오프라인)

## 포털 구성

| 영역 | 설명 |
|------|------|
| 홈 `/` | 카테고리 허브, 추천·최근 공고, 뉴스, 상품 배너 |
| 수급공고 `/listings` | 진행중/오늘마감/마감 · side·카테고리·지역 필터 |
| 공고 상세 `/listings/:id` | 비로그인 열람, 연락처는 로그인 후 |
| 뉴스·공지 `/news` | PortalArticle |
| 상품신청 `/products` | 유료 노출 신청(PG 없음, 운영자 승인) |
| 내 공고 `/listings/mine` | 마감 포함 |

MES(`farmCode`)는 선택 연동입니다. EggFactory를 쓰지 않는 농가도 수동 공고로 참여합니다.

## 알림 (NotifyPort → 향후 회사 Notify API)

관심분야·수신동의 후 신규 공고 매칭 시 **앱 알림 + SMS + 알림톡**을 `NotifyPort`로 발송합니다.

- 패키지: `com.poultry.platform.notify`
- 채널: `IN_APP` / `SMS`(Solapi) / `ALIMTALK`(Solapi 카카오)
- 발송 감사 로그: `notify_delivery_logs`
- 체험 API: `POST /api/notify/v1/messages` (JWT, 향후 API Key 멀티테넌트로 확장)

`application.properties`에서 Solapi/알림톡 키를 넣으면 실제 발송, 비우면 dry-run 로그만 남깁니다.

```properties
app.notify.solapi.enabled=true
app.notify.solapi.api-key=...
app.notify.solapi.api-secret=...
app.notify.solapi.sender=010xxxxxxxx
app.notify.kakao.enabled=true
app.notify.kakao.pf-id=...
app.notify.kakao.template-id=...
```

회원가입 시 **관심 분야 1개 이상** + **SMS/알림톡 중 1개 이상 동의** + **휴대폰**이 필요합니다.

## 기술 스택

- Backend: Spring Boot 3.5 · Java 17 · Gradle · MariaDB
- Frontend: React 19 · Vite · TypeScript

## DB

- Host: `localhost:3306` / DB: `poultry` / User: `root` / Password: `1111`

## 실행

```bat
start-all.bat
```

또는

```bat
start-backend.bat
start-frontend.bat
```

브라우저: http://localhost:5173

## 시드 계정

| 아이디 | 비밀번호 | 역할 |
|--------|----------|------|
| admin | admin1234 | 운영자 (기사·상품승인) |
| farm1 | farm1234 | 농가 · MES (`FARM-GB-001`) |
| farm2 | farm1234 | 농가 · MES 미연동 |
| dealer1 | dealer1234 | 계란 파트너 |

시드 상품: `FEATURED_LISTING`, `HOME_BANNER`  
시드에 추천 노출된 데모 공고·수요 공고가 함께 생성됩니다. (DB가 비어 있을 때)

## MES mock

```http
POST /api/integrations/mes/listings
X-MES-API-KEY: mes-dev-api-key-change-me
```

`farmCode=FARM-GB-001` (farm1) 로만 매핑됩니다.
