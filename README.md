# 통합 피드백 게시판

부산대 AI융합교육원 5개 시스템의 피드백을 한 곳에 모아 보는 사이트.

## 대상 사이트
| 코드 | 이름 |
|---|---|
| codeplace | 코드플레이스 |
| aipms | AIPMS |
| aicms | AICMS |
| aicap | AI역량지원시스템 |
| srvadm | pickle (서버관리) |

## 기술
- 백엔드: Spring Boot 3.x / JDK 21 / JPA / Spring Security / PostgreSQL
- 프론트: React (Vite)

## 폴더 구조
```
backend/    스프링부트 (서버)
frontend/   React (화면)
docs/       기획 문서, 화면 그림
```

## 처음 받는 사람

```bash
git clone https://github.com/PNUFeedbackBoard/feedback-board.git
```

설치해야 할 것: JDK 21, IntelliJ, Node.js(LTS), VS Code
자세한 건 `docs/setup-guide.md`

## 브랜치 규칙

```
main      배포된 것만. 직접 push 금지
develop   평소 작업이 모이는 곳
feature/  각자 작업 브랜치
```

### 흐름
```
feature/내작업  ->  develop  ->  main
                   PR         배포할 때만
```

### 작업할 때
```bash
git checkout develop
git pull
git checkout -b feature/feedback-write   # 본인 작업 이름
# ... 작업 ...
git add .
git commit -m "feat: 피드백 작성 화면"
git push -u origin feature/feedback-write
```
그다음 GitHub에서 PR 올린다. **받는 쪽(base)을 `develop`으로** 둔다.
2명이 확인하면 합친다.

### 브랜치 이름
- `feature/영문-소문자-하이픈` 예: `feature/admin-page`
- 한글, 띄어쓰기 안 씁니다

### 커밋 메시지
```
feat: 새 기능
fix: 버그 수정
docs: 문서
chore: 설정, 잡일
```

## 팀
5명. 문진혁, (팀장), (팀원 3명)
