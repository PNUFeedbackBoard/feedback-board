// 경로: /admin
// 전체 현황 대시보드. 개발자·열람자 공용이며 조회 전용이다. 기획 6-2 참고.
// TODO(C, 5단계): 상태별 건수 카드, 차트 4종(프로젝트별 누적 막대 · 유형별 도넛 · 기간별 추이 선 ·
//                 평균 처리 소요 시간), 우선 처리 요청 목록, 최근 접수 10건을 채운다.
//                 데이터는 endpoints.js 의 getDashboard() 한 번으로 모두 받는다. 차트는 recharts 를 쓴다.
export default function DashboardPage() {
  return (
    <main>
      <h1>전체 현황 대시보드</h1>
      <p>5단계 · C 담당</p>
      <p>5개 프로젝트를 합친 지표를 보는 조회 전용 화면이다.</p>
    </main>
  )
}
