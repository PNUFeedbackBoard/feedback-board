// 경로: /admin
// 전체 현황 대시보드. 개발자·열람자 공용이며 조회 전용이다. 기획 6-2 참고.
// TODO(C, 5단계): 상태별 건수 카드, 차트 4종(프로젝트별 누적 막대 · 유형별 도넛 · 기간별 추이 선 ·
//                 평균 처리 소요 시간), 우선 처리 요청 목록, 최근 접수 10건을 채운다.
//                 데이터는 endpoints.js 의 getDashboard() 한 번으로 모두 받는다. 차트는 recharts 를 쓴다.
export default function DashboardPage() {
  return (
		<section className="dashboard-intro">
			<p className="dashboard-intro__eyebrow">ALL PROJECTS · OVERVIEW</p>
			<h1>
				더 나은 캠퍼스를 만드는 생각,
				<br />
				여기서 다음 개선으로 이어집니다.
			</h1>
			<p className="dashboard-intro__copy">
				5개 프로젝트의 피드백 흐름을 한곳에서 살펴보세요. 아래 프로젝트 디스크에서
				작업할 보드를 선택할 수 있습니다.
			</p>
			<div className="dashboard-intro__meta" aria-label="서비스 소개">
				<span>INI</span>
				<span>ISSUE &amp; IDEA</span>
				<span>PUSAN NATIONAL UNIVERSITY</span>
			</div>
		</section>
  )
}
