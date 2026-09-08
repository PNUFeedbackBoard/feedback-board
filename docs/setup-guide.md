# 개발환경 설정 가이드

## 1. 필수 프로그램

- Git
- JDK 21
- Node.js LTS
- IntelliJ IDEA 또는 VS Code
- Windows에서는 PowerShell 또는 명령 프롬프트

PostgreSQL은 운영 환경에서 사용합니다. 현재 로컬 개발은 별도 설치가 필요 없는 H2 메모리 데이터베이스로 실행됩니다.

## 2. 저장소 받기

```powershell
git clone https://github.com/PNUFeedbackBoard/feedback-board.git
cd feedback-board
```

작업 전 `develop` 브랜치에서 최신 내용을 받습니다.

```powershell
git switch develop
git pull origin develop
```

## 3. 프론트엔드 설정

```powershell
cd frontend
Copy-Item .env.example .env.local
npm install
npm run dev
```

프론트엔드는 `http://localhost:5173`에서 실행됩니다. `/api` 요청은 기본적으로 `http://localhost:8080`으로 전달됩니다. 다른 백엔드 주소를 사용할 때는 `frontend/.env.local`의 `VITE_API_PROXY_TARGET` 값을 변경합니다.

## 4. 백엔드 설정

새 터미널에서 저장소 루트의 `backend` 폴더로 이동합니다.

```powershell
cd backend
.\gradlew.bat bootRun
```

백엔드는 `http://localhost:8080`에서 실행됩니다.

현재 로컬 기본값은 다음과 같습니다.

- 데이터베이스: H2 메모리 DB
- JDBC URL: `jdbc:h2:mem:feedback`
- H2 콘솔: `http://localhost:8080/h2-console`
- H2 콘솔 JDBC URL: `jdbc:h2:mem:feedback`
- 사용자: `sa`
- 비밀번호: 없음

H2 메모리 DB는 백엔드를 재시작하면 데이터가 초기화됩니다.

## 5. 한 번에 실행하기

저장소 루트에서 다음을 실행하면 백엔드와 프론트엔드가 각각 새 창에서 실행됩니다.

```powershell
.\run.bat
```

## 6. 검사 명령

프론트엔드 린트:

```powershell
cd frontend
npm run lint
```

프론트엔드 빌드:

```powershell
npm run build
```

백엔드 테스트:

```powershell
cd ..\backend
.\gradlew.bat test
```

## 7. 작업 브랜치 규칙

`develop`에서 기능 브랜치를 만들고, 작업이 끝나면 `develop`을 대상으로 PR을 생성합니다.

```powershell
git switch develop
git pull origin develop
git switch -c feature/작업이름
```

커밋 메시지는 다음 형식을 사용합니다.

```text
feat: 새 기능
fix: 버그 수정
docs: 문서 수정
chore: 설정 및 유지보수
```

## 8. 환경변수 주의사항

- 실제 비밀번호와 API 키는 `.env.local` 또는 `application-local.properties`에만 둡니다.
- `.env.example`만 Git에 커밋합니다.
- `.gitignore`에 의해 로컬 환경 파일은 커밋되지 않습니다.

## Docker PostgreSQL

Docker Desktop을 설치한 환경에서는 PostgreSQL 컨테이너를 실행할 수 있습니다.

```powershell
cd ..
docker compose up -d postgres
cd backend
.\gradlew.bat bootRun --args="--spring.profiles.active=docker"
```

컨테이너를 중지하려면 다음을 실행합니다.

```powershell
docker compose down
```

## API 문서와 오류 응답

백엔드 실행 후 다음 주소에서 API 문서를 확인할 수 있습니다.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

API 오류는 `timestamp`, `status`, `code`, `message`, `path`, `details`를 포함하는 공통 JSON 형식으로 반환됩니다. 입력 검증 오류의 경우 `details`에 필드별 오류가 담깁니다.

## GitHub Actions

`.github/workflows/ci.yml`에서 `develop` 또는 `main`에 push하거나 PR을 생성할 때 다음 검사를 자동 실행합니다.

- 백엔드: Java 21로 Gradle 테스트
- 프론트엔드: Node.js 24로 의존성 설치, Oxlint, Vite 빌드
