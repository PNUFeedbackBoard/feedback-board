import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getAdminFeedbacks } from '../../api/endpoints.js'
import { AUTHOR_TYPE, BOARD_COLUMN_STATUSES, FEEDBACK_STATUS, labelOf } from '../../constants/enums.js'
import CategoryTag from './components/CategoryTag.jsx'
import PriorityChip from './components/PriorityChip.jsx'
import PriorityRequestBadge from './components/PriorityRequestBadge.jsx'
import StatusBadge from './components/StatusBadge.jsx'
import './board.css'

/**
 * 개발 보드 — 칸반. 기획 6-3
 *
 * 1단계 범위는 **골격**이다. 스텁 응답으로 3열과 반영 불가 영역이 표시되는 것까지가 완료 조건이다.
 *
 * TODO(C, 3단계): 카드 상세 패널(전체 내용, 유형·중요도 변경, 답변 화면 이동)을 붙인다.
 * TODO(C, 4단계): @dnd-kit 으로 열 간 이동을 붙이고 updateAdminFeedback(id, { status }) 로 연결한다.
 * TODO(C, 5단계): 상단에 정렬·유형·기간 필터를 배치한다. 정렬은 각 열 내부에 적용한다.
 */
export default function BoardPage() {
	const { projectCode } = useParams()
	// 응답이 어느 프로젝트의 것인지 함께 담아 둔다. 그래야 프로젝트를 옮긴 직후에
	// effect 안에서 상태를 되돌리지 않고도 이전 응답을 화면에서 걸러 낼 수 있다.
	const [state, setState] = useState({ projectCode: null, items: [], message: '' })

	useEffect(() => {
		let cancelled = false

		// 0-1단계 스텁은 필터를 무시하고 14건을 전부 내려준다.
		// 계약대로 project 를 보내 두면 B 의 1단계 작업이 끝나는 순간 화면 수정 없이 걸러진다.
		getAdminFeedbacks({ project: projectCode })
			.then((page) => {
				if (!cancelled) setState({ projectCode, items: page.items, message: '' })
			})
			.catch((error) => {
				if (!cancelled) setState({ projectCode, items: [], message: error.message })
			})

		return () => {
			cancelled = true
		}
	}, [projectCode])

	if (state.projectCode !== projectCode) return <p className="board__notice">불러오는 중…</p>
	if (state.message) return <p className="board__notice">{state.message}</p>

	const rejected = state.items.filter((item) => item.status === 'REJECTED')

	return (
		<div className="board">
			<div className="board__columns">
				{BOARD_COLUMN_STATUSES.map((status) => {
					// 서버가 정렬해 내려준 순서를 그대로 쓴다. 우선 처리 요청 항목이 최상단에 오는 것도
					// 서버가 정한다(기획 4-5). 화면에서 다시 정렬하지 않는다.
					const items = state.items.filter((item) => item.status === status)

					return (
						<section key={status} className="board__column">
							<header className="board__column-head">
								<StatusBadge status={status} />
								<span className="board__count">{items.length}</span>
							</header>

							<div className="board__cards">
								{items.map((item) => (
									<FeedbackCard key={item.id} item={item} />
								))}
								{items.length === 0 && <p className="board__empty">항목이 없습니다</p>}
							</div>
						</section>
					)
				})}
			</div>

			{/* 반영 불가는 열을 차지하지 않고 보드 하단에 접은 상태로 둔다. 기획 6-3 */}
			<details className="board__rejected">
				<summary>
					{labelOf(FEEDBACK_STATUS, 'REJECTED')}
					<span className="board__count">{rejected.length}</span>
				</summary>
				<div className="board__cards board__cards--row">
					{rejected.map((item) => (
						<FeedbackCard key={item.id} item={item} />
					))}
					{rejected.length === 0 && <p className="board__empty">항목이 없습니다</p>}
				</div>
			</details>
		</div>
	)
}

/**
 * 카드 표시 항목은 기획 6-3 이 정한 7가지다.
 * 우선 처리 요청 배지 · 중요도 · 제목 · 유형 · 경과일 · 회원 여부 · 답변 여부
 */
function FeedbackCard({ item }) {
	return (
		<article className="card" data-priority-requested={item.priorityRequested}>
			{item.priorityRequested && <PriorityRequestBadge />}

			<h3 className="card__title">{item.title}</h3>

			<div className="card__tags">
				<PriorityChip priority={item.priority} />
				<CategoryTag category={item.category} />
			</div>

			<footer className="card__meta">
				<span>{daysSince(item.createdAt)}일 경과</span>
				<span>{labelOf(AUTHOR_TYPE, item.authorType)}</span>
				<span>{item.answered ? '답변 완료' : '답변 없음'}</span>
			</footer>
		</article>
	)
}

/**
 * 등록일로부터 며칠 지났는지. 기획 6-3 의 "경과일" 이다.
 * createdAt 은 시간대 없는 LocalDateTime 문자열이라 브라우저 현지 시각으로 해석된다.
 */
function daysSince(createdAt) {
	const MS_PER_DAY = 24 * 60 * 60 * 1000
	return Math.max(0, Math.floor((Date.now() - new Date(createdAt).getTime()) / MS_PER_DAY))
}
