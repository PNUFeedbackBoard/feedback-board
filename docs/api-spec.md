# API 명세

프런트엔드와 백엔드가 공유하는 제품 API 계약이다. 아래 12개 API는 Swagger에 노출되며 실제 DB 데이터를 조회·저장한다.

## 사용자 API

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/projects` | 공개 | 프로젝트 목록 |
| `POST` | `/api/feedbacks` | 공개 | 회원·비회원 피드백 등록 |
| `GET` | `/api/me/feedbacks` | 로그인 | 내 문의 목록 |
| `GET` | `/api/me/feedbacks/{id}` | 로그인 | 내 문의 상세와 답변 |

## 관리 API

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/admin/dashboard` | `DEVELOPER`, `VIEWER` | 전체 현황 지표 |
| `GET` | `/api/admin/feedbacks` | `DEVELOPER`, `VIEWER` | 필터·정렬·페이징 목록 |
| `GET` | `/api/admin/feedbacks/{id}` | `DEVELOPER`, `VIEWER` | 관리 상세 |
| `PATCH` | `/api/admin/feedbacks/{id}` | `DEVELOPER` | 상태·유형·중요도·담당자 변경 |
| `PUT` | `/api/admin/feedbacks/{id}/answer` | `DEVELOPER` | 답변 등록·수정 |
| `POST` | `/api/admin/feedbacks/{id}/priority-request` | `VIEWER` | 우선 처리 요청 토글 |
| `GET` | `/api/admin/users` | `DEVELOPER` | 계정 목록 |
| `PATCH` | `/api/admin/users/{id}` | `DEVELOPER` | 계정 역할·승인 상태 변경 |

## 인증 보조 API

화면이 서버가 판정한 현재 계정과 역할을 조회할 수 있도록 `GET /api/me`를 제공한다. 이 인증 보조 API와 아래 개발 전용 API는 제품 API 12개를 보여 주는 Swagger 목록에서는 숨긴다.

`dev` 프로필에서만 다음 API를 제공한다.

- `POST /api/dev/login`: `{ "account": "dev" }` 형식으로 `dev`, `viewer`, `user`, `pending` 중 하나를 선택한다.
- `POST /api/dev/logout`: 현재 세션을 종료한다.

## 권한 계약

- 공개: 프로젝트 목록과 피드백 등록
- `/api/me/**`: 로그인 필요
- `/api/admin/**` 조회: `DEVELOPER` 또는 `VIEWER`
- 관리 데이터 변경: `DEVELOPER`
- 우선 처리 요청: `VIEWER`
- `VIEWER`가 피드백 변경 API를 호출하면 403

관리 목록은 `project`, `status`, `category`, `sort`, `from`, `to`, `authorType`, `answered`, `page`, `size` 쿼리를 지원한다. 어떤 정렬에서도 우선 처리 요청이 먼저 나오며 `size`는 1~100이다.

피드백 변경 본문은 `status`, `category`, `priority`, `assigneeId`, `unassign` 중 필요한 값만 보낸다. 담당자를 지정할 때는 활성 개발자 계정의 `assigneeId`를 보내고, 해제할 때는 `unassign: true`를 보낸다.
