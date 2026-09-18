import { useEffect, useState } from 'react'
import { useOutletContext, useParams } from 'react-router-dom'
import { getAdminFeedbacks, updateAdminFeedback } from '../../api/endpoints.js'
import AssigneeDialog from './AssigneeDialog.jsx'
import FeedbackCard from './components/FeedbackCard.jsx'
import FilterBar from './components/FilterBar.jsx'
import './intake.css'

/**
 * 접수 — 아직 손대지 않은 피드백을 훑고 거르는 화면이다.
 *
 * **팀 확인이 필요한 기획 변경이다.** 기획 6-1 의 상단 탭은 개발 보드·답변 두 개이고
 * 6-3 의 칸반은 진행 전·진행 중·완료 세 열이다. 접수를 칸반에서 떼어 탭으로 올렸다.
 * 접수는 훑고 거르는 일이고 개발 보드는 진행 상황을 보는 일이라 성격이 다르며,
 * 한 화면에 두면 접수가 쌓일수록 진행 중인 일이 밀려난다.
 *
 * 필터 항목은 기획 4-5 를 따른다. 사이트는 하단 디스크가, 진행 상태는 이 탭 자체가 정하므로
 * 유형·기간·회원 여부와 정렬만 둔다.
 */
export default function IntakePage() {
	const { projectCode } = useParams()
	const { assignees, setAssignees } = useOutletContext()
	const [filters, setFilters] = useState({ sort: 'PRIORITY' })
	const [state, setState] = useState({ projectCode: null, items: [], message: '' })
	const [pending, setPending] = useState(null)
	const [notice, setNotice] = useState('')

	useEffect(() => {
		let cancelled = false

		// 계약대로 필터를 서버에 보낸다. B 의 1단계 작업이 끝나면 서버가 걸러서 내려준다.
		getAdminFeedbacks({ ...filters, project: projectCode, status: 'RECEIVED' })
			.then((page) => {
				if (!cancelled) setState({ projectCode, items: page.items, message: '' })
			})
			.catch((error) => {
				if (!cancelled) setState({ projectCode, items: [], message: error.message })
			})

		return () => {
			cancelled = true
		}
	}, [projectCode, filters])

	/** 처리 중으로 넘긴다. 담당자를 함께 적어 두면 개발 보드에서 누가 맡았는지 보인다. */
	function startWork(id, assignee) {
		const before = state.items
		setState((prev) => ({ ...prev, items: prev.items.filter((item) => item.id !== id) }))
		setAssignees((prev) => ({ ...prev, [id]: assignee }))
		setNotice('')

		updateAdminFeedback(id, { status: 'IN_PROGRESS' }).catch((error) => {
			setState((prev) => ({ ...prev, items: before }))
			setNotice(`처리 중으로 옮기지 못했습니다. ${error.message}`)
		})
	}

	if (state.projectCode !== projectCode) return <p className="board__notice">불러오는 중…</p>
	if (state.message) return <p className="board__notice">{state.message}</p>

	// TODO(C, 2단계): B 의 필터·정렬이 붙으면 이 두 줄을 지운다.
	//                 0-1단계 스텁이 파라미터를 무시하고 14건을 그대로 내려주기 때문에 둔 임시 처리다.
	//                 서버가 이미 걸러 보내면 같은 조건이라 결과가 달라지지 않는다.
	const visible = sortItems(state.items.filter((item) => matches(item, filters)), filters.sort)

	return (
		<div className="intake">
			<FilterBar value={filters} onChange={setFilters} />

			{notice && <p className="board__alert">{notice}</p>}

			<p className="intake__count">
				접수 <strong>{visible.length}</strong>건
			</p>

			{visible.length === 0 ? (
				<p className="board__empty">조건에 맞는 접수 건이 없습니다.</p>
			) : (
				<div className="intake__list">
					{visible.map((item) => (
						<FeedbackCard
							key={item.id}
							item={item}
							assignee={assignees[item.id]}
							action={
								<button
									type="button"
									className="intake__start"
									onClick={() => setPending({ id: item.id, title: item.title })}
								>
									처리 시작
								</button>
							}
						/>
					))}
				</div>
			)}

			{pending && (
				<AssigneeDialog
					feedbackTitle={pending.title}
					onCancel={() => setPending(null)}
					onConfirm={(name) => {
						startWork(pending.id, name)
						setPending(null)
					}}
				/>
			)}
		</div>
	)
}

/** 스텁이 필터를 무시하는 동안 화면에서 같은 조건으로 거른다. */
function matches(item, filters) {
	// 이 탭은 접수만 다룬다. 스텁이 status 를 무시하고 모든 상태를 내려주므로 여기서 거른다.
	if (item.status !== 'RECEIVED') return false
	if (filters.category && item.category !== filters.category) return false
	if (filters.authorType && item.authorType !== filters.authorType) return false
	// createdAt 은 'YYYY-MM-DDTHH:mm:ss' 라 앞 10글자가 날짜다. 문자열 비교로 충분하다.
	const day = item.createdAt.slice(0, 10)
	if (filters.from && day < filters.from) return false
	if (filters.to && day > filters.to) return false
	return true
}

/** 정렬은 기획 4-5 를 따른다. 어떤 기준을 골라도 우선 처리 요청이 1순위다. */
function sortItems(items, sort) {
	const RANK = { HIGH: 0, NORMAL: 1, LOW: 2 }

	return [...items].sort((a, b) => {
		if (a.priorityRequested !== b.priorityRequested) return a.priorityRequested ? -1 : 1
		if (sort === 'OLDEST') return a.createdAt.localeCompare(b.createdAt)
		if (sort === 'LATEST') return b.createdAt.localeCompare(a.createdAt)
		// 중요도순. 같은 중요도면 최신순이다.
		if (RANK[a.priority] !== RANK[b.priority]) return RANK[a.priority] - RANK[b.priority]
		return b.createdAt.localeCompare(a.createdAt)
	})
}
