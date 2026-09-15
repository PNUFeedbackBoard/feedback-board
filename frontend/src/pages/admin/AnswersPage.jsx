import { useParams } from 'react-router-dom'

// 경로: /admin/:projectCode/answers
// 답변 탭. 기획 6-4 참고.
// TODO(C, 3단계): 답변 대기열 목록을 채운다. 기본 필터는 `답변 필요`, 기본 정렬은 오래된순(OLDEST)이다.
//                 비회원(GUEST) 항목은 비활성으로 표시하고 답변 버튼을 주지 않는다.
//                 답변 저장은 upsertAnswer(id, { content, markDone }) 를 쓴다.
export default function AnswersPage() {
  const { projectCode } = useParams()

  return (
    <main>
      <h1>답변</h1>
      <p>3단계 · C 담당</p>
      <p>프로젝트: {projectCode}</p>
    </main>
  )
}
