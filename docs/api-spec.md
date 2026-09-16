# API 스텁 명세

0-1단계의 화면 병렬 개발을 위한 계약이다. 제품 API 12개는 Swagger에 노출하고 모두 정상 요청에 목 데이터와 HTTP 200을 반환한다. 개발 전용 로그인은 Swagger에서 숨긴다.

## 사용자·인증 API

| 메서드 | 경로 | 설명 |
|---|---|---|
| `GET` | `/api/auth/me` | 서버가 판정한 현재 사용자와 역할 조회 |
| `POST` | `/api/feedbacks` | 회원·비회원 피드백 등록 |
| `GET` | `/api/feedbacks/my` | 내 문의 목록 조회 |
| `GET` | `/api/feedbacks/my/{feedbackId}` | 내 문의 상세 조회 |
| `POST` | `/api/feedbacks/{feedbackId}/priority-request` | 우선 처리 요청 |

## 관리 API

| 메서드 | 경로 | 설명 |
|---|---|---|
| `GET` | `/api/admin/feedbacks` | 필터·정렬·페이징된 관리 목록 조회 |
| `GET` | `/api/admin/feedbacks/{feedbackId}` | 관리 상세 조회 |
| `PATCH` | `/api/admin/feedbacks/{feedbackId}` | 상태·유형·개발자 중요도 변경 |
| `PUT` | `/api/admin/feedbacks/{feedbackId}/answer` | 답변 등록·수정 |
| `GET` | `/api/admin/dashboard` | 6개 대시보드 지표 조회 |
| `GET` | `/api/admin/accounts` | 계정 목록 조회 |
| `PATCH` | `/api/admin/accounts/{accountId}` | 계정 역할·승인 상태 변경 |

## 개발 전용 로그인

`dev` 프로필에서만 `POST /api/dev/login`을 제공한다.

```json
{ "email": "dev@demo.local" }
```

지원 이메일은 `dev@demo.local`, `viewer@demo.local`, `user@demo.local`이다. 로그아웃/비회원 상태로 바꾸려면 `email`을 `null`로 보낸다. 브라우저 세션 쿠키를 유지한 상태에서 `/api/auth/me`와 관리 API를 호출한다.

## 권한 계약

- `/api/feedbacks/**`: 공개
- `/api/auth/**`: 로그인 필요
- `/api/admin/**` GET: `DEVELOPER` 또는 `VIEWER`
- `/api/admin/**` 변경: `DEVELOPER`만 허용하며 `VIEWER`는 403
- Swagger, OpenAPI JSON, 개발용 H2 콘솔: 공개
