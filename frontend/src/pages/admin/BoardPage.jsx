import { useParams } from 'react-router-dom'

// 경로: /admin/:projectCode/board
// 개발 보드 — 칸반. 기획 6-3 참고.
// TODO(C, 1단계): 진행 전·진행 중·완료 3열 보드와 하단의 반영 불가 접힘 영역을 골격으로 만든다.
//                 열 구성은 constants/enums.js 의 BOARD_COLUMN_STATUSES 를 쓴다.
//                 드래그는 @dnd-kit 을 쓰고, 상태 변경 연결은 4단계에서 한다.
// TODO(C, 3단계): 카드 상세 패널(전체 내용, 유형·중요도 변경, 답변 화면 이동)을 붙인다.
export default function BoardPage() {
  const { projectCode } = useParams()

  return (
    <main>
      <h1>개발 보드</h1>
      <p>1단계 · C 담당</p>
      <p>프로젝트: {projectCode}</p>
    </main>
  )
}
