# AI로 작업할 때 지키는 수칙

이 파일은 **AI 도구(Claude Code, Cursor, Copilot 등)가 읽는 작업 규칙**이다.
4명이 각자 다른 부분을 AI로 구현하기 때문에, 이 규칙을 지키지 않으면 합칠 때 깨진다.

사람이 읽는 기획 문서는 [docs/planning.md](docs/planning.md) 다.

---

## 0. 작업 시작 전에 반드시 할 것

```
1. docs/planning.md 를 읽는다. (특히 4장 규칙, 8장 데이터 모델, 9장 API, 13장 개발 순서)
2. 내 담당 영역이 어디인지 아래 "담당 경계"에서 확인한다.
3. git switch develop && git pull 로 최신을 받는다.
4. feature/{영역}-{작업} 브랜치를 만든다.
5. 지금 하려는 작업이 몇 단계 작업인지 확인한다. 앞 단계가 끝나지 않았으면 시작하지 않는다.
```

AI에게 일을 시킬 때는 **위 1번을 AI에게도 시킨다.** "docs/planning.md 를 먼저 읽어라"를 프롬프트에 넣는다.

---

## 1. 절대 규칙

| 규칙 | 이유 |
|---|---|
| **응답 JSON 필드명을 바꾸지 않는다** | 프론트가 그 이름에 의존한다. 바꾸려면 팀에 먼저 말한다 |
| **다른 담당의 폴더를 수정하지 않는다** | 같은 파일을 동시에 고치면 합칠 때 충돌한다 |
| **화면에서 역할(Role)을 판단하지 않는다** | 서버가 내려준 역할 값만 쓴다. 7단계 인증 전환의 전제다 |
| **화면에 색상·여백을 직접 쓰지 않는다** | 공용 컴포넌트와 토큰만 쓴다. 안 그러면 두 화면이 다른 서비스로 보인다 |
| **지금 단계에 없는 것을 구현하지 않는다** | AI는 시키지 않은 것까지 만든다. 다음 단계 담당자의 일을 뺏지 않는다 |
| **생성된 코드를 이해하지 못한 채 커밋하지 않는다** | 설명하지 못하는 코드는 리뷰도 수정도 못 한다 |

---

## 2. 담당 경계

| 기호 | 담당 | 만질 수 있는 폴더 |
|---|---|---|
| A | 백엔드 · 도메인과 사용자용 API | `backend/.../domain/`, `backend/.../feedback/` |
| B | 백엔드 · 관리용 API와 인프라 | `backend/.../admin/`, `backend/.../auth/`, `backend/.../config/`, `application*.properties` |
| C | 프론트 · 관리용 화면 | `frontend/src/pages/admin/` |
| D | 디자인 · 사용자용 화면 | `frontend/src/pages/user/`, `frontend/src/components/common/`, `frontend/src/styles/` |

### 공유 파일 — 함부로 열지 않는다

| 파일 | 소유 | 수정이 필요하면 |
|---|---|---|
| `config/SecurityConfig.java` | B | 0-1단계에서 경로를 모두 선언했다. 다시 열 일이 없어야 한다 |
| `domain/` 엔티티, 리포지토리 | A | B는 조회 메서드만 추가하고 커밋 전에 공유한다 |
| `frontend/src/routes.jsx` | 0-1 산출물 | 모든 경로가 이미 등록돼 있다. 추가하지 않는다 |
| `frontend/src/components/common/` | D | D에게 요청한다. 대기가 길면 내 화면 안에 임시로 만들고 통합 때 넘긴다 |
| `frontend/src/constants/enums.js` | 0-1 산출물 | 백엔드 enum과 쌍이다. 한쪽만 고치지 않는다 |
| `package.json`, `build.gradle` | 0-1 산출물 | 라이브러리는 0단계에 일괄 설치했다. 추가는 팀에 먼저 말한다 |

---

## 3. AI에게 일을 시킬 때

### 프롬프트에 반드시 넣을 것

```
1. "docs/planning.md 를 먼저 읽어라"
2. 내 담당 폴더 경로 — "이 폴더 밖의 파일은 수정하지 마라"
3. 지금 단계 — "이것은 N단계 작업이다. 그 범위를 넘지 마라"
4. 완료 조건 — docs/planning.md 13-5절의 내 칸에 적힌 문장을 그대로 붙인다
```

### 프롬프트에 넣으면 안 되는 것

- "알아서 잘 만들어줘" — AI가 계약에 없는 필드명을 지어낸다
- "기능을 완성해줘" — 다음 단계 담당자의 일까지 만들어버린다
- 계약과 다른 필드명을 즉석에서 지어내는 것

### AI가 만든 결과를 받은 뒤

```
1. 어떤 파일이 새로 생겼는지 git status 로 확인한다
2. 내 담당 폴더 밖의 파일이 바뀌었으면 되돌린다 — git checkout -- <경로>
3. 응답 필드명이 계약과 같은지 확인한다
4. 내가 설명할 수 있는 코드인지 확인한다. 모르는 부분은 AI에게 설명을 요청한다
5. 빌드와 린트를 돌린다 (아래 4번)
```

---

## 4. 작업을 끝내기 전에

### 백엔드

```bash
cd backend
./gradlew test
```

### 프론트엔드

```bash
cd frontend
npm run lint
npm run build
```

둘 다 통과하지 않으면 PR을 올리지 않는다.

### 체크리스트

- [ ] 내 담당 폴더 밖의 파일이 바뀌지 않았다 (`git status` 로 확인)
- [ ] 응답 필드명이 계약과 같다
- [ ] 화면에 색상 값을 직접 쓰지 않았다
- [ ] 화면에서 역할을 판단하지 않았다
- [ ] 시키지 않은 기능이 딸려 들어오지 않았다
- [ ] 생성된 코드를 내가 설명할 수 있다

---

## 5. 코드 규칙

### 백엔드

- 패키지 루트는 `kr.ac.pusan.feedback`
- enum은 `@Enumerated(EnumType.STRING)` 으로 저장한다
- 테이블명은 복수형. `user` 는 예약어라 반드시 `users` 를 쓴다
- 응답 DTO는 `record` 로 만든다
- Lombok은 `@Getter` 만 쓴다. `@Setter` 는 쓰지 않는다
- 관리용과 사용자용의 응답 DTO를 공용으로 쓰지 않는다
  (`AdminFeedbackResponse` 와 `MyFeedbackResponse` 를 따로 둔다. 사용자에게 내부 필드가 나가면 안 된다)
- 검증은 `jakarta.validation` 어노테이션으로 한다. 오류 응답 형식은 `GlobalExceptionHandler` 가 이미 정해뒀다

### 프론트엔드

- API 호출은 `src/api/endpoints.js` 를 통해서만 한다. 컴포넌트에서 `fetch` 를 직접 쓰지 않는다
- 상태·유형·중요도의 한국어 표기는 `src/constants/enums.js` 에서 가져온다. 문자열을 화면에 직접 쓰지 않는다
- 색상·간격은 `src/styles/tokens.css` 의 변수만 쓴다. `#hex` 나 `padding: 12px` 를 직접 쓰지 않는다
- 상태 배지와 중요도 칩은 `components/common` 의 것을 쓴다. 새로 만들지 않는다

### 주석

- 한국어로 쓴다
- 미완성 지점은 담당과 단계를 적는다

```java
// TODO(B, 1단계): 실제 필터 조회로 교체
```

---

## 6. 개발 중 인증 (7단계 전까지)

구글 로그인은 **7단계에서** 붙인다. 그 전까지는 데모 계정으로 개발한다.

| 계정 | 역할 | 확인 대상 |
|---|---|---|
| `dev@demo.local` | DEVELOPER | 관리용 화면 전체 |
| `viewer@demo.local` | VIEWER | 조회 권한, 우선 처리 요청, 수정 시 403 |
| `user@demo.local` | USER | 사용자용 홈, 내 문의 |
| `pending@demo.local` | USER · PENDING | 승인 대기 화면 |
| 로그인 안 함 | — | 비회원 등록 경로 |

- 전환은 개발 모드에서만 보이는 계정 전환 위젯으로 한다
- `POST /api/dev/login` 은 **`dev` 프로필에서만** 등록된다. 운영에서는 404다
- **이 장치에 의존하는 코드를 화면에 쓰지 마라.** 화면은 `GET /api/me` 가 내려준 역할만 본다

---

## 7. 브랜치와 커밋

```
feature/{영역}-{작업}  →  develop  →  main
```

- 브랜치 이름은 영문 소문자와 하이픈만. 예: `feature/admin-board`
- `develop` 에 직접 push 하지 않는다
- PR의 base는 `develop`. 2명이 확인하면 합친다
- 매일 `develop` 최신을 내 브랜치에 반영한다

커밋 메시지

```
feat: 새 기능
fix: 버그 수정
docs: 문서
chore: 설정, 잡일
```

---

## 8. 흔한 사고와 예방

| 사고 | 언제 생기나 | 예방 |
|---|---|---|
| 합칠 때 충돌 | 두 사람이 같은 파일을 고침 | 담당 폴더 밖을 건드리지 않는다 |
| 화면이 갑자기 깨짐 | 백엔드가 필드명을 바꿈 | 계약 변경은 반드시 먼저 공유한다 |
| 두 화면이 달라 보임 | 각자 색을 직접 지정함 | 토큰과 공용 컴포넌트만 쓴다 |
| 7단계에서 화면을 전부 고침 | 화면이 역할을 자체 판단함 | 서버가 준 역할만 쓴다 |
| 남의 일을 먼저 해버림 | AI에게 "완성해줘"라고 시킴 | 단계와 범위를 프롬프트에 명시한다 |
| DB가 초기화됨 | H2 인메모리라 재시작 시 사라짐 | 정상 동작이다. 시드가 매번 다시 들어간다 |

---

## 9. 환경

- 백엔드: JDK 21, Spring Boot 3.5.6, 로컬은 H2 인메모리 (별도 DB 설치 불필요)
- 프론트: Node LTS, React 19, Vite
- H2 콘솔: http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:feedback`, 사용자 `sa`, 비밀번호 없음
- Swagger: http://localhost:8080/swagger-ui.html
- 프론트 개발 서버: http://localhost:5173 — `/api` 요청은 8080으로 프록시된다

macOS는 `./gradlew`, Windows는 `.\gradlew.bat` 를 쓴다.
`run.bat` 은 Windows 전용이다. macOS·Linux는 터미널 두 개를 열어 각각 실행한다.
