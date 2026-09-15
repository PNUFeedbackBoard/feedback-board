import { useParams } from 'react-router-dom'

// 경로: /my/:id
// 문의 상세. 회원 전용. 기획 5-3 참고.
// TODO(D, 5단계): 제목·내용·유형·등록일·상태 배지·개발자 답변을 채운다.
//                 답변이 없을 때와 조회에 실패했을 때의 안내 문구도 함께 만든다.
//                 조회는 endpoints.js 의 getMyFeedback(id) 를 쓴다. 작성자는 수정·삭제할 수 없다.
export default function MyFeedbackDetailPage() {
  const { id } = useParams()

  return (
    <main>
      <h1>문의 상세</h1>
      <p>5단계 · D 담당</p>
      <p>문의 번호: {id}</p>
    </main>
  )
}
