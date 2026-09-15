import { Link } from 'react-router-dom'

// 등록되지 않은 모든 경로가 여기로 온다. routes.jsx 의 마지막 항목이다.
// 0-1단계 공통 기반 산출물이다. 화면 담당이 따로 없으므로 그대로 두어도 된다.
export default function NotFoundPage() {
  return (
    <main>
      <h1>페이지를 찾을 수 없습니다</h1>
      <p>주소를 다시 확인해 주세요.</p>
      <Link to="/">처음 화면으로</Link>
    </main>
  )
}
