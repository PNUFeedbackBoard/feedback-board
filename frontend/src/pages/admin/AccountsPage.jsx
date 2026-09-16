// 경로: /admin/accounts
// 계정 관리. 개발자 전용. 기획 6-5 참고.
// TODO(D, 6단계): 승인 대기 목록(이메일·이름·가입일)과 역할 지정·승인·거절을 채운다.
//                 기존 계정의 역할 변경과 비활성화도 여기서 처리한다.
//                 조회는 getAdminUsers(), 변경은 updateAdminUser(id, { role, status }) 를 쓴다.
export default function AccountsPage() {
  return (
    <main>
      <h1>계정 관리</h1>
      <p>6단계 · D 담당</p>
      <p>개발자가 대기 계정을 승인하고 역할을 부여하는 화면이다.</p>
    </main>
  )
}
