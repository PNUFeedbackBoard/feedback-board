# 공용 배지

최신 기준은 [카드·배지 디자인](../../../../docs/design/README.md)이다.

| 디자인 이름 | 컴포넌트 | 사용법 |
|---|---|---|
| Card/Badge/Process | StatusBadge | `<StatusBadge status="RECEIVED" />` |
| Card/Badge/Urgent | PriorityChip | `<PriorityChip priority="HIGH" />` |
| Card/Badge/Sort | CategoryTag | `<CategoryTag category="BUG" />` |
| Card/Badge/Important | PriorityRequestBadge | `<PriorityRequestBadge />` |

영문 코드와 props 이름을 유지하고, 한국어 표시는 `src/constants/enums.js`의 label을 사용한다. 공용 스타일은 `feedback-badges.css`, 디자인 값은 `styles/tokens.css`의 `--feedback-*` 토큰을 사용한다.

현재 중요도 하나를 외곽선 칩으로 표시한다. 우선 처리 요청은 보라색 별로 표시하며 접근성 이름을 제공한다. 관리자 화면의 기존 배지 파일들은 공용 컴포넌트를 재내보낸다.
