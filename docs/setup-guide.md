# 개발환경 설정 가이드

macOS와 Windows를 함께 적는다. 명령이 다르면 둘 다 표시한다.

---

## 1. 설치할 것

| 프로그램 | 버전 | 비고 |
|---|---|---|
| Git | 최신 | |
| JDK | **21** | 다른 버전이면 빌드가 안 된다 |
| Node.js | LTS | |
| IntelliJ IDEA 또는 VS Code | | |

**PostgreSQL은 설치하지 않는다.** 로컬 개발은 H2 인메모리 DB로 돌아간다.
H2는 따로 설치하는 프로그램이 아니라 백엔드가 켜질 때 메모리에 잠깐 만들어지는 DB다.
PostgreSQL은 나중에 배포(8단계)에서만 쓴다.

### macOS에서 JDK 21 설치

```bash
brew install openjdk@21
```

Homebrew의 openjdk는 **keg-only**라서 설치해도 `java` 명령이 바로 잡히지 않는다.
`~/.zshrc` 에 아래를 추가하고 터미널을 새로 연다.

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
export PATH="$JAVA_HOME/bin:$PATH"
```

확인:

```bash
java -version
```

`openjdk version "21..."` 이 나와야 한다.
`Unable to locate a Java Runtime` 이 나오면 위 두 줄이 적용되지 않은 것이다.

### Windows에서 JDK 21 설치

Temurin 또는 Oracle JDK 21을 설치하고 `java -version` 으로 확인한다.

---

## 2. 저장소 받기

```bash
git clone https://github.com/PNUFeedbackBoard/feedback-board.git
cd feedback-board
git switch develop
git pull origin develop
```

---

## 3. 백엔드 실행

### macOS · Linux

```bash
cd backend
./gradlew bootRun
```

`permission denied` 가 나오면 실행 권한이 없는 것이다.

```bash
chmod +x gradlew
```

### Windows

```powershell
cd backend
.\gradlew.bat bootRun
```

백엔드는 http://localhost:8080 에서 뜬다.
**첫 실행은 Gradle과 라이브러리를 받느라 몇 분 걸릴 수 있다.** 두 번째부터는 10초 안쪽이다.

### 기본 설정

옵션 없이 실행하면 `dev` 프로필로 뜬다.

- DB: H2 인메모리 (`jdbc:h2:mem:feedback`)
- 서버를 내리면 **데이터가 사라진다. 정상이다.** 다시 켜면 시드 데이터가 새로 들어간다
- 테이블은 엔티티를 보고 자동으로 만들어진다 (`ddl-auto=create-drop`)

---

## 4. H2 콘솔로 DB 안 보기

http://localhost:8080/h2-console

### 주의 3가지

**1. `./gradlew bootRun` 으로 띄웠을 때만 열린다.**
`java -jar` 로 실행하면 404다. H2 콘솔은 개발 도구(devtools)가 켜 주는 것이기 때문이다.

**2. 접속 화면에 미리 채워진 주소를 반드시 지운다.**
H2 콘솔은 JDBC URL 칸에 `jdbc:h2:~/test` 를 미리 채워 둔다.
이건 우리 DB가 아니라 홈 폴더에 새 파일 DB를 만드는 주소다.
**지우고 아래 값을 직접 넣는다.**

| 칸 | 값 |
|---|---|
| JDBC URL | `jdbc:h2:mem:feedback` |
| 사용자명 | `sa` |
| 비밀번호 | (비움) |

그냥 Connect를 누르면 연결은 되는데 테이블이 하나도 없다. 이때 "DB가 비었다"고 오해하기 쉽다.

**3. 파일을 저장하면 DB가 초기화된다.**
개발 도구가 코드 변경을 감지해 앱을 자동 재시작하는데, 그때 메모리 DB도 함께 비워지고 시드가 다시 들어간다.
"방금 넣은 데이터가 사라졌다"면 대부분 이 경우다.

---

## 5. 프론트엔드 실행

새 터미널을 연다.

### macOS · Linux

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

### Windows

```powershell
cd frontend
Copy-Item .env.example .env.local
npm install
npm run dev
```

프론트엔드는 http://localhost:5173 에서 뜬다.
`/api` 요청은 `http://localhost:8080` 으로 전달된다.
다른 백엔드 주소를 쓰려면 `frontend/.env.local` 의 `VITE_API_PROXY_TARGET` 을 바꾼다.

`git pull` 로 새 라이브러리가 들어왔을 수 있으니 **받을 때마다 `npm install` 을 다시 돌린다.**

---

## 6. 한 번에 실행하기

### Windows

저장소 루트에서:

```powershell
.\run.bat
```

### macOS · Linux

`run.bat` 은 Windows 전용이다. 터미널 두 개를 열어 각각 실행한다.

```bash
# 터미널 1
cd backend && ./gradlew bootRun
```

```bash
# 터미널 2
cd frontend && npm run dev
```

---

## 7. 개발 중 로그인

구글 로그인은 **7단계에서** 붙인다. 그 전까지는 데모 계정으로 개발한다.

화면 오른쪽 아래에 개발 모드에서만 보이는 계정 전환 위젯이 있다. 거기서 역할을 바꾼다.

| 계정 | 역할 | 볼 수 있는 것 |
|---|---|---|
| `dev@demo.local` | 개발자 | 관리용 화면 전체 |
| `viewer@demo.local` | 열람자 | 조회, 우선 처리 요청 (수정하면 403) |
| `user@demo.local` | 일반 사용자 | 사용자용 홈, 내 문의 |
| `pending@demo.local` | 승인 대기 | 승인 대기 안내 화면 |

이 로그인은 `dev` 프로필에서만 동작한다. 운영 프로필에서는 경로 자체가 없어 404다.

---

## 8. 검사 명령

PR을 올리기 전에 둘 다 통과해야 한다.

### 백엔드

```bash
cd backend
./gradlew test
```

Windows는 `.\gradlew.bat test`

### 프론트엔드

```bash
cd frontend
npm run lint
npm run build
```

---

## 9. API 문서

백엔드를 띄운 뒤:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

API 오류는 `timestamp`, `status`, `code`, `message`, `path`, `details` 를 담은 공통 JSON 형식으로 온다.
입력 검증에 실패하면 `details` 에 어떤 필드가 왜 틀렸는지 들어간다.

---

## 10. 작업 브랜치

```bash
git switch develop
git pull origin develop
git switch -c feature/작업이름
```

커밋 메시지 형식:

```
feat: 새 기능
fix: 버그 수정
docs: 문서 수정
chore: 설정 및 유지보수
```

AI 도구로 작업할 때 지켜야 하는 규칙은 [CLAUDE.md](../CLAUDE.md) 에 있다. 시작하기 전에 읽는다.

---

## 11. 환경변수 주의사항

- 실제 비밀번호와 API 키는 `.env.local` 또는 `application-local.properties` 에만 둔다
- `.env.example` 만 커밋한다
- `.gitignore` 가 로컬 환경 파일을 제외한다

---

## 12. Docker PostgreSQL (지금은 필요 없음)

배포 전에 PostgreSQL로 미리 확인하고 싶을 때만 쓴다. **0단계에서는 필요 없다.**

Docker Desktop이 설치된 환경에서:

```bash
docker compose up -d postgres
cd backend
./gradlew bootRun --args="--spring.profiles.active=docker"
```

중지:

```bash
docker compose down
```

---

## 13. CI

`.github/workflows/ci.yml` 이 `develop` · `main` 으로 push하거나 PR을 만들 때 자동 실행된다.

- 백엔드: Java 21로 Gradle 테스트
- 프론트엔드: Node.js 24로 의존성 설치, Oxlint, Vite 빌드

CI는 `bash gradlew` 로 실행하므로 **실행 권한 문제가 CI에서는 드러나지 않는다.**
"CI는 통과하는데 내 맥에서만 안 되는" 경우 3번의 `chmod +x gradlew` 를 확인한다.
