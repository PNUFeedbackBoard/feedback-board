# 학교 서버 운영 배포 가이드

이 문서는 통합 피드백 게시판을 부산대학교 서버와 실제 도메인에 배포할 때 사용하는 실행 계획이다.
처음 배포하는 사람이나 AI가 기존 구현 의도를 다시 추측하지 않고, 위에서 아래로 확인하며 진행할 수 있도록 작성했다.

> 운영 원칙: 프론트엔드와 백엔드를 하나의 HTTPS 도메인으로 제공하고, 비밀값은 서버 환경변수로만 주입한다.
> 운영 서버에서는 `dev` 프로필과 Vite 개발 서버를 절대 사용하지 않는다.

## 1. 현재 구현에서 이미 보장하는 것

- 회원 로그인은 Google OAuth 2.0을 사용한다.
- 로그인 콜백에서 이메일 도메인과 Google Workspace `hd` 클레임이 모두 정확히 `pusan.ac.kr`인지 서버가 검사한다.
- `hd=pusan.ac.kr` 요청값은 계정 선택을 돕는 힌트이며, 실제 접근 통제는 백엔드에서 수행한다.
- 일반 Gmail이나 다른 조직 계정은 사용자 레코드와 로그인 세션을 만들지 않는다.
- 회원은 자신의 피드백만 조회할 수 있고 비회원 피드백은 다시 조회할 수 없다.
- 인증 상태는 `JSESSIONID` 기반 서버 세션으로 유지된다.
- 로컬 `dev` 프로필만 H2와 데모 로그인을 사용한다. 운영은 PostgreSQL 프로필을 사용해야 한다.

관련 구현과 상세 정책:

- [Google 로그인 설정](google-login.md)
- [개발환경 설정](setup-guide.md)
- `backend/src/main/java/kr/ac/pusan/feedback/config/SecurityConfig.java`
- `backend/src/main/java/kr/ac/pusan/feedback/auth/GoogleOAuth2SuccessHandler.java`

## 2. 배포 전에 학교 담당자와 확정할 항목

아래 표를 먼저 채운다. 값이 정해지기 전에는 OAuth 클라이언트나 인증서를 확정하지 않는다.

| 항목 | 예시 | 실제 값 |
|---|---|---|
| 서비스 도메인 | `feedback.example.pusan.ac.kr` | `미정` |
| 서버 OS와 버전 | Ubuntu 24.04 LTS | `미정` |
| 외부 공개 포트 | 443, 필요 시 80 | `미정` |
| 내부 Spring 포트 | 8080 | `미정` |
| PostgreSQL 위치 | 같은 서버 또는 학교 DB 서버 | `미정` |
| TLS 인증서 발급 주체 | 학교 전산원 또는 Let's Encrypt | `미정` |
| Google Cloud 프로젝트 소유자 | 학교 Workspace 조직 또는 팀 계정 | `미정` |
| 운영 관리자 이메일 | `관리자@pusan.ac.kr` | `미정` |
| 비밀값 보관 위치 | 학교 Secret Manager 또는 서버 환경 파일 | `미정` |
| 백업 저장 위치와 보존 기간 | 별도 스토리지, 30일 | `미정` |
| 장애 연락 담당자 | 이름/연락처 | `미정` |

학교 네트워크 정책도 확인한다.

- 외부 사용자가 인터넷에서 접속할 수 있는가?
- Google 로그인 도메인으로 나가는 HTTPS 통신이 허용되는가?
- 서버가 SMTP 서버에 접근할 수 있는가?
- 방화벽에서 80/443만 공개하고 8080과 5432는 외부에서 차단할 수 있는가?
- 학교가 제공하는 DNS, TLS 인증서, 리버스 프록시 또는 컨테이너 운영 규칙이 있는가?

## 3. 권장 운영 구조

프론트와 백엔드를 같은 출처(origin)로 제공한다.

```text
사용자 브라우저
    |
    | HTTPS :443
    v
https://feedback.example.pusan.ac.kr
    |
    +-- /, /home, /write, /admin/**  -> React 정적 파일 + SPA fallback
    +-- /api/**                       -> Spring Boot :8080
    +-- /oauth2/**                    -> Spring Boot :8080
    +-- /login/oauth2/**              -> Spring Boot :8080
                                         |
                                         +-- PostgreSQL :5432
                                         +-- Google OAuth HTTPS
                                         +-- SMTP(선택)
```

이 구조를 권장하는 이유:

- 로그인 세션 쿠키가 프론트와 API에서 자연스럽게 공유된다.
- 별도 CORS와 cross-site 쿠키 설정이 필요하지 않다.
- OAuth 리다이렉션 URI가 하나의 공개 도메인으로 고정된다.
- 백엔드의 8080 포트를 인터넷에 직접 공개하지 않을 수 있다.

프론트와 API를 서로 다른 도메인에 배치하려면 CORS, credential 요청, `SameSite=None; Secure` 쿠키와 CSRF 정책을 별도로 설계해야 한다. 현재 구현의 운영 기본안이 아니므로 특별한 이유가 없다면 사용하지 않는다.

## 4. 운영 환경변수

다음 값은 배포 플랫폼의 Secret 기능, systemd의 읽기 제한된 환경 파일, Docker secret 등으로 주입한다.
실제 값이 들어간 파일은 저장소에 커밋하지 않는다.

```dotenv
# 운영에서는 dev가 아닌 프로필을 명시한다.
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080

# 리버스 프록시가 전달한 원래 HTTPS 주소를 Spring이 인식하게 한다.
SERVER_FORWARD_HEADERS_STRATEGY=framework

# Google OAuth: Google Cloud Console에서 발급한 운영용 값
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID=<GOOGLE_CLIENT_ID>
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET=<GOOGLE_CLIENT_SECRET>

# 브라우저에 공개되는 실제 HTTPS 주소. 끝에 /를 붙이지 않는다.
FRONTEND_URL=https://<SERVICE_DOMAIN>

# 최초 운영 관리자. 반드시 @pusan.ac.kr 계정으로 지정한다.
BOOTSTRAP_DEVELOPER_EMAIL=<ADMIN_EMAIL>@pusan.ac.kr

# PostgreSQL
DB_URL=jdbc:postgresql://<DB_HOST>:5432/<DB_NAME>
DB_USERNAME=<DB_USER>
DB_PASSWORD=<STRONG_DB_PASSWORD>

# 초기 배포는 update를 사용할 수 있지만, 안정화 후 Flyway 도입과 validate 전환을 권장한다.
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

# 메일 알림을 실제로 사용할 때만 설정한다.
SPRING_MAIL_HOST=<SMTP_HOST>
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=<SMTP_USERNAME>
SPRING_MAIL_PASSWORD=<SMTP_PASSWORD_OR_APP_PASSWORD>
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
MAIL_FROM=<MAIL_FROM_ADDRESS>
```

주의 사항:

- 환경변수 관리 화면에는 일반적으로 바깥쪽 따옴표를 넣지 않는다.
- Google client secret, DB 비밀번호, SMTP 비밀번호를 React의 `VITE_*` 변수에 넣지 않는다. `VITE_*` 값은 빌드 결과에 노출될 수 있다.
- 운영 서버의 환경 파일을 사용한다면 소유자를 서비스 계정으로 제한하고 권한을 `600` 수준으로 설정한다.
- 운영 로그에 OAuth 코드, 토큰, 쿠키, 비밀번호가 출력되지 않는지 확인한다.

## 5. Google Cloud Console 운영 설정

### 5-1. OAuth 클라이언트

애플리케이션 유형은 `웹 애플리케이션`으로 만든다. 승인된 리디렉션 URI에 다음 값을 정확히 등록한다.

```text
https://<SERVICE_DOMAIN>/login/oauth2/code/google
```

예시:

```text
https://feedback.example.pusan.ac.kr/login/oauth2/code/google
```

스킴, 호스트, 포트, 경로와 마지막 슬래시 여부가 실제 요청과 완전히 같아야 한다. 운영 주소는 HTTPS를 사용한다.
로컬 개발도 같은 클라이언트를 사용한다면 아래 주소를 함께 둘 수 있지만, 가능하면 개발용과 운영용 Google Cloud 프로젝트/클라이언트를 분리한다.

```text
http://localhost:8080/login/oauth2/code/google
```

이 프로젝트는 서버 측 OAuth 흐름을 사용하므로 핵심 설정은 승인된 리디렉션 URI다. 프론트에서 Google JavaScript SDK를 직접 사용하도록 구조를 바꾸기 전에는 승인된 JavaScript 원본에 의존하지 않는다.

### 5-2. 동의 화면과 게시 상태

- 학교 Google Workspace 조직이 소유한 Cloud 프로젝트라면 `Internal` 앱 사용 가능 여부를 학교 관리자에게 확인한다.
- 팀 개인 프로젝트라 `External`만 가능하면 여러 학생이 사용할 수 있도록 앱을 테스트 상태에서 운영 상태로 전환한다.
- 테스트 상태를 유지하면 테스트 사용자 목록 때문에 실제 학생 로그인 범위가 제한될 수 있다.
- 요청 scope는 현재 `openid`, `email`, `profile`만 사용한다. 필요하지 않은 Google API scope를 추가하지 않는다.
- 앱 이름, 지원 이메일, 홈페이지, 개인정보처리방침과 서비스 도메인을 실제 운영 정보로 채운다.

중요: Google 화면의 `hd` 설정만 믿지 않는다. `GoogleOAuth2SuccessHandler`의 서버 검증은 제거하지 않는다.

## 6. PostgreSQL 준비

운영에서는 H2를 사용하지 않는다. H2 개발 데이터는 서버 재시작 시 사라질 수 있으며 실제 사용자 데이터를 보존하기에 적합하지 않다.

최소 작업:

1. 운영 전용 DB와 최소 권한 DB 사용자를 만든다.
2. PostgreSQL 5432 포트는 애플리케이션 서버에서만 접근 가능하게 제한한다.
3. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 서버 비밀값으로 등록한다.
4. 배포 전 빈 DB에서 애플리케이션 기동과 테이블 생성을 검증한다.
5. 매일 자동 백업과 실제 복구 연습을 구성한다.

권장 백업 정책 예시:

- 매일 논리 백업 또는 관리형 DB 스냅샷
- 최근 7일 일간 백업, 최근 4주 주간 백업 보관
- 애플리케이션 서버와 다른 저장소에 백업
- 학기 시작 전 또는 대규모 배포 전 수동 스냅샷
- 최소 분기 1회 복구 테스트

스키마 관리 계획:

- 최초 제한적 배포: 현재 기본값인 `JPA_DDL_AUTO=update` 사용 가능
- 정식 운영 전: Flyway 마이그레이션 파일 도입
- Flyway 도입 후: `JPA_DDL_AUTO=validate`로 변경
- 운영 DB에 수동으로 스키마를 변경했다면 반드시 같은 내용을 마이그레이션 파일에 반영

## 7. 빌드와 배포 절차

배포 대상 커밋을 고정하고 깨끗한 체크아웃에서 수행한다.

### 7-1. 배포 전 검사

Windows 개발 환경:

```powershell
cd backend
.\gradlew.bat test

cd ..\frontend
npm ci
npm run lint
npm run build
```

Linux CI/서버:

```bash
cd backend
./gradlew test bootJar

cd ../frontend
npm ci
npm run lint
npm run build
```

결과물:

- 백엔드: `backend/build/libs/*.jar`
- 프론트엔드: `frontend/dist/`

운영 서버에서 소스 전체를 직접 빌드하기보다 CI에서 검증된 산출물을 만들어 배포하는 방식을 권장한다.

### 7-2. 서버 배치 예시

```text
/opt/feedback-board/
    app/feedback.jar
    releases/<commit-sha>/feedback.jar
    releases/<commit-sha>/frontend/
    current -> releases/<commit-sha>/

/etc/feedback-board/
    feedback.env       # 비밀값 포함, Git에 없음

/var/log/feedback-board/
```

새 버전은 새 release 디렉터리에 올리고 검증 후 `current` 링크를 전환한다. 이전 release를 일정 개수 보존하면 장애 시 빠르게 되돌릴 수 있다.

### 7-3. systemd 예시

서버 사용자와 경로는 학교 운영 규칙에 맞게 바꾼다.

```ini
[Unit]
Description=PNU Feedback Board API
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=feedback
Group=feedback
WorkingDirectory=/opt/feedback-board/current
EnvironmentFile=/etc/feedback-board/feedback.env
ExecStart=/usr/bin/java -jar /opt/feedback-board/current/feedback.jar
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

## 8. Nginx 리버스 프록시 예시

다음은 구조를 설명하는 예시다. 인증서 경로와 보안 헤더는 학교 표준을 우선한다.

```nginx
server {
    listen 80;
    server_name <SERVICE_DOMAIN>;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name <SERVICE_DOMAIN>;

    ssl_certificate     /path/to/fullchain.pem;
    ssl_certificate_key /path/to/privkey.pem;

    root /opt/feedback-board/current/frontend;
    index index.html;

    location ~ ^/(api|oauth2|login/oauth2)/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Port $server_port;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

확인할 점:

- `/api`, `/oauth2`, `/login/oauth2`가 모두 Spring으로 전달되는가?
- React Router 경로를 새로고침해도 `index.html`이 반환되는가?
- 외부에서 8080과 5432에 직접 접근할 수 없는가?
- HTTP가 HTTPS로 전환되는가?
- Spring이 OAuth 콜백 주소를 `https://...`로 생성하는가?

학교가 Nginx 대신 Apache, Kubernetes Ingress 또는 자체 L7 장비를 제공하면 같은 경로 규칙으로 변환한다.

## 9. 최초 배포 순서

1. 배포할 Git 커밋 SHA와 담당자를 기록한다.
2. DNS에 서비스 도메인을 연결한다.
3. TLS 인증서를 설치하고 HTTPS 접속을 먼저 확인한다.
4. PostgreSQL DB, 사용자, 방화벽과 백업을 구성한다.
5. Google OAuth 운영 클라이언트와 동의 화면을 설정한다.
6. 실제 운영 콜백 URI를 Google Cloud Console에 등록한다.
7. 운영 환경변수와 비밀값을 서버에 등록한다.
8. 백엔드 JAR와 프론트 `dist`를 배포한다.
9. `SPRING_PROFILES_ACTIVE=docker`로 백엔드를 시작한다.
10. Nginx 설정을 검사하고 적용한다.
11. 아래 smoke test를 수행한다.
12. 정상 동작 확인 후 배포 기록에 커밋 SHA, 시간, 담당자, DB 변경 여부를 남긴다.

최초 관리자 절차:

1. `BOOTSTRAP_DEVELOPER_EMAIL`에 지정한 부산대 계정으로 처음 로그인한다.
2. 관리 화면 접근과 `DEVELOPER`, `ACTIVE` 상태를 확인한다.
3. 다른 관리자의 관리용 로그인을 받고 계정 관리 화면에서 역할과 상태를 지정한다.
4. 최소 두 명의 관리자 계정을 확보한다.
5. 부트스트랩 이메일을 계속 유지할지 제거할지 운영 정책으로 결정한다. 제거해도 이미 생성된 사용자의 역할은 DB에 남는다.

## 10. 배포 후 smoke test

시크릿이나 개인정보를 화면 캡처와 로그에 남기지 않는다.

### 공개 화면

- [ ] `https://<SERVICE_DOMAIN>/` 접속 성공
- [ ] `/home`, `/write`, `/admin` 직접 새로고침 시 404가 아닌 React 화면 표시
- [ ] HTTP 접속 시 HTTPS로 이동
- [ ] 모바일 폭에서 로그인 화면과 계정 배지가 깨지지 않음

### 로그인과 권한

- [ ] `@pusan.ac.kr` 계정 로그인 성공
- [ ] 로그인 후 오른쪽 상단에 현재 이름과 이메일 표시
- [ ] 일반 `@gmail.com` 계정 로그인 거부
- [ ] 거부 후 다시 Google 로그인을 눌러 다른 계정 선택 가능
- [ ] 로그아웃 후 `/api/auth/me`가 401 반환
- [ ] 일반 사용자가 `/admin` 데이터에 접근하지 못함
- [ ] 회원 A가 회원 B의 피드백을 조회하지 못함
- [ ] 비회원 작성은 가능하지만 회원의 내 문의 목록에 섞이지 않음

### 관리자

- [ ] 최초 개발자 로그인과 관리 화면 접근 성공
- [ ] `PENDING` 관리 계정 승인과 역할 지정 가능
- [ ] `VIEWER`가 수정 요청을 하면 403
- [ ] `DISABLED` 계정 로그인 거부

### 데이터와 운영

- [ ] 서버 재시작 후 사용자와 피드백 데이터 유지
- [ ] DB 백업 생성 성공
- [ ] 애플리케이션 로그에 비밀번호, OAuth 코드, 쿠키가 없음
- [ ] SMTP를 설정했다면 테스트 알림의 링크가 실제 HTTPS 도메인을 가리킴
- [ ] `/api/dev/login`과 `/api/dev/logout`이 운영에서 404
- [ ] H2 Console이 운영에서 비활성화됨

## 11. 다중 사용자와 확장 계획

### 11-1. 서버 한 대

초기 운영은 Spring 서버 한 대와 PostgreSQL로 시작할 수 있다. 현재의 메모리 기반 HTTP 세션도 이 구조에서는 동작한다.

운영 전에 최소한 다음을 설정한다.

- JVM 메모리 제한과 서버 자원 모니터링
- 애플리케이션 재시작 정책
- 로그 순환과 디스크 사용량 경보
- DB 연결 수와 느린 쿼리 모니터링
- 5xx, 로그인 실패율, 응답 시간 경보

### 11-2. Spring 서버 여러 대

서버가 여러 인스턴스로 늘어나면 현재 메모리 세션은 인스턴스끼리 공유되지 않는다. 확장 전에 다음 중 하나를 적용한다.

1. 권장: Spring Session + Redis로 `JSESSIONID` 세션 공유
2. 임시: 로드밸런서 sticky session 사용

Redis 세션을 도입할 때 확인할 것:

- 모든 인스턴스가 같은 Redis에 접근
- Redis 장애와 재시작 시 로그인 세션이 사라질 수 있음을 운영 정책에 반영
- 세션 TTL과 로그아웃 동작 확인
- Redis는 외부 인터넷에 공개하지 않음
- 배포 중 구버전과 신버전의 세션 직렬화 호환성 확인

정적 프론트 파일은 Nginx 외에도 학교 CDN이나 정적 호스팅으로 분리할 수 있지만, 브라우저가 보는 공개 origin은 가능하면 동일하게 유지한다.

## 12. 배포, 장애 대응과 롤백 기록

매 배포마다 다음 내용을 기록한다.

```text
배포 일시:
담당자:
Git 커밋 SHA:
배포 전 버전:
DB 마이그레이션:
환경변수 변경:
Google OAuth 설정 변경:
smoke test 결과:
롤백 기준과 결과:
특이사항:
```

롤백 원칙:

1. 로그인, 권한, 데이터 저장에 문제가 있으면 신규 트래픽을 우선 차단하거나 점검 화면으로 전환한다.
2. DB 변경이 없는 배포는 이전 JAR와 프론트 release로 되돌리고 서비스를 재시작한다.
3. DB 변경이 있으면 마이그레이션의 하위 호환성과 백업 시점을 먼저 확인한다.
4. 데이터를 되돌리는 작업은 삭제나 손실 위험이 있으므로 백업을 보존하고 학교 운영 담당자 승인 후 수행한다.
5. 롤백 후 부산대 로그인, 본인 글 조회, 관리자 권한, 새 피드백 저장을 다시 검사한다.

## 13. 정식 운영 전에 남은 개선 작업

우선순위가 높은 순서다.

- [ ] 실제 도메인, 서버, DB, 인증서 담당자 확정
- [ ] 개발/운영 Google Cloud 프로젝트 또는 OAuth 클라이언트 분리
- [ ] 개인정보처리방침, 서비스 이용 안내, 문의처 게시
- [ ] PostgreSQL 자동 백업과 복구 훈련
- [ ] Flyway 도입 후 `ddl-auto=validate` 전환
- [ ] 상태 확인용 health endpoint와 학교 모니터링 연동
- [ ] 중앙 로그 수집과 개인정보 마스킹 확인
- [ ] 관리자 최소 2명 지정 및 계정 중지 절차 문서화
- [ ] 부하 테스트로 동시 사용자와 DB 연결 수 산정
- [ ] 다중 인스턴스가 필요해지는 시점에 Redis 세션 도입
- [ ] 보안 점검: 의존성 취약점, HTTP 헤더, 세션 쿠키, CSRF, 권한 API 재검증
- [ ] 장애 공지, 데이터 보존 기간, 탈퇴 요청 처리 정책 결정

## 14. AI 또는 다음 담당자가 작업을 시작할 때

다음 순서로 현재 상태를 다시 확인한다.

1. 이 문서의 `배포 전에 학교 담당자와 확정할 항목`에 실제 값이 채워졌는지 본다.
2. `application.properties`, `application-docker.properties`, `SecurityConfig`, `GoogleOAuth2SuccessHandler`를 읽어 문서와 코드가 달라지지 않았는지 확인한다.
3. 실제 비밀값을 코드, Markdown, 로그, 이슈 또는 PR에 복사하지 않는다.
4. 운영 인프라가 Nginx/systemd인지 Docker/Kubernetes인지 확인한 뒤 해당 방식의 설정 파일을 만든다.
5. 배포 전에 전체 백엔드 테스트와 프론트 lint/build를 실행한다.
6. Google OAuth의 실제 리다이렉션 URI와 `FRONTEND_URL`이 같은 공개 HTTPS 도메인을 가리키는지 확인한다.
7. 배포 후 이 문서의 smoke test를 전부 수행하고 결과를 배포 기록에 남긴다.

## 15. 공식 참고 자료

- [Google OAuth 웹 서버 애플리케이션](https://developers.google.com/identity/protocols/oauth2/web-server)
- [Google OAuth 운영 준비와 게시 상태](https://developers.google.com/identity/protocols/oauth2/production-readiness/overview)
- [Spring Boot 외부 설정](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Spring Boot 프록시 전달 헤더](https://docs.spring.io/spring-boot/how-to/webserver.html#howto.webserver.use-behind-a-proxy-server)
- [Spring Session](https://docs.spring.io/spring-session/reference/)
- [PostgreSQL 백업과 복원](https://www.postgresql.org/docs/current/backup.html)
