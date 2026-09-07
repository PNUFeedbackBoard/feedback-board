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
- `main` 에 직접 push 하지 않는다
- 작업할 때 브랜치를 만든다: `feature/내작업이름`
- PR 올리고 2명이 확인하면 합친다

## 팀
5명. 문진혁, (팀장), (팀원 3명)
