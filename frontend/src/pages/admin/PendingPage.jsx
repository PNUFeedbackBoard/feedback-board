// 경로: /admin/pending
// 승인 대기 안내. 기획 3-2 참고.
// TODO(D, 6단계): 계정 상태가 PENDING 인 동안 보여줄 안내 문구를 채운다.
//                 이 화면에서는 다른 기능을 열어 주지 않는다. getMe() 의 status 로 진입 여부를 판단한다.
export default function PendingPage() {
  return (
    <main>
      <h1>승인 대기</h1>
      <p>6단계 · D 담당</p>
      <p>구글 로그인은 되었지만 아직 개발자의 승인을 받지 못한 계정에 보여줄 화면이다.</p>
    </main>
  )
}
