// 경로: /home
// 홈 — 내 문의 목록. 회원 전용. 기획 5-3 참고.
// TODO(D, 3단계): `새 질문 적기` 버튼과 본인 피드백 목록을 채운다.
//                 목록은 endpoints.js 의 getMyFeedbacks() 를 쓰고, 비어 있으면 안내 문구를 보인다.
export default function HomePage() {
  return (
    <main>
      <h1>홈 — 내 문의</h1>
      <p>3단계 · D 담당</p>
      <p>회원이 자신이 등록한 피드백을 확인하는 화면이다.</p>
    </main>
  )
}
