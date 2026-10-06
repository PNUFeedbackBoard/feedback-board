# 구글 로그인 설정

구글 연동 설정이 없어도 dev 프로필로 실행하고 권한 미리보기를 사용할 수 있다.

1. Google Cloud Console에서 Google Auth Platform 동의 화면을 설정한다.
2. 외부 앱이 테스트 상태라면 팀원 이메일을 테스트 사용자에 추가한다.
3. 웹 애플리케이션 OAuth 클라이언트를 만든다.
4. 승인된 리디렉션 URI: `http://localhost:8080/login/oauth2/code/google`.
5. 배포 시 실제 백엔드의 `https://도메인/login/oauth2/code/google`도 등록한다.

발급된 ID와 비밀은 저장소에 커밋하지 않고 백엔드 환경 변수에 넣는다.

```sh
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID='발급된 ID'
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET='발급된 비밀'
export BOOTSTRAP_DEVELOPER_EMAIL='최초 개발자의 구글 이메일'
export FRONTEND_URL='http://localhost:5173'
cd backend
./gradlew bootRun
```

Spring Google 기본 scope(openid/profile/email)를 사용한다. ID와 secret을 모두 설정한다.
로컬에서는 프론트 주소를 localhost:5173으로 통일한다. 127.0.0.1과 혼용하면 쿠키가 공유되지 않는다.
Vite는 /api, /oauth2, /login/oauth2를 백엔드로 프록시한다. 배포는 같은 사이트의 리버스 프록시에서
이 경로를 백엔드로 전달하고 나머지는 SPA로 제공한다. HTTPS와 전달 헤더는 배포 환경에 맞춰 설정한다.
FRONTEND_URL은 신뢰하는 고정 주소만 사용한다.

- 일반 Google 로그인: 신규 USER·ACTIVE, 바로 내 문의 이용.
- 관리용 Google 로그인: 신규 USER·PENDING, 개발자가 역할 지정 및 승인.
- 최초 개발자: 지정 이메일의 신규 가입만 DEVELOPER·ACTIVE. 기존 계정 역할/상태는 보존.
- DISABLED와 다른 구글 sub가 이미 연결된 이메일의 로그인은 거부.
- 승인/역할 변경 후 다시 로그인해 새 권한을 세션에 반영한다.
- 로그아웃은 앱 세션만 종료한다. 구글 계정 자체는 로그아웃되지 않는다.
- ?site=aipms 같은 출처는 일반 로그인 후 작성 화면까지 유지한다.

개발용 미리보기는 별도의 데모 계정으로 전환한다. 실제 계정의 역할을 수정하지 않는다.
구글 계정으로 돌아가려면 로그아웃 후 Google 로그인한다.
프론트 개발 모드 AND 백엔드 dev 프로필에서만 미리보기가 보인다.
운영은 반드시 dev 이외 프로필을 명시하고 Vite 개발 서버를 배포하지 않는다.

확인: 설정 없이 버튼 비활성/미리보기 동작 → 자격 증명 설정 후 실제 로그인 →
다른 신규 계정 관리용 가입/승인/재로그인 → 열람자 수정403 → 중지 계정 로그인 거부 →
로그아웃 후 me401 → CSRF 없는 변경403 → 운영 미리보기 비노출 및 demo API 없음.
자동 테스트는 모의 구글 응답을 사용하며 실제 동의 화면/콜백은 발급된 자격 증명으로 별도 확인한다.

참고: [Google OIDC](https://developers.google.com/identity/openid-connect/openid-connect),
[Spring CSRF](https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html).

Swagger 변경 요청도 CSRF가 필요하다. 같은 브라우저에서 `/api/auth/csrf`의 token을 복사해 Authorize에 입력한다.
