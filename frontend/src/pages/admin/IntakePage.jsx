import { useEffect, useState } from 'react'
import { useOutletContext, useParams } from 'react-router-dom'
import {
	getAdminFeedbacks,
	togglePriorityRequest,
	updateAdminFeedback,
} from '../../api/endpoints.js'
import { FEEDBACK_STATUS, labelOf } from '../../constants/enums.js'
import AssigneeDialog from './AssigneeDialog.jsx'
import DetailPanel from './DetailPanel.jsx'
import FeedbackCard from './components/FeedbackCard.jsx'
import FilterBar from './components/FilterBar.jsx'
import Pagination from './components/Pagination.jsx'
import './intake.css'

/**
 * 접수 — 아직 손대지 않은 피드백을 훑고 거르는 화면이다.
 *
 * **팀 확인이 필요한 기획 변경이다.** 기획 6-1 의 상단 탭은 개발 보드·답변 두 개이고
 * 6-3 의 칸반은 진행 전·진행 중·완료 세 열이다. 접수를 칸반에서 떼어 탭으로 올렸다.
 * 접수는 훑고 거르는 일이고 개발 보드는 진행 상황을 보는 일이라 성격이 다르며,
 * 한 화면에 두면 접수가 쌓일수록 진행 중인 일이 밀려난다.
 *
 * 상태를 바꾸는 길이 둘이다.
 *   - 여러 건을 체크해 한꺼번에 처리 시작. 훑고 골라내는 주 동선이다.
 *   - 카드마다의 선택 박스. 한 건을 반영 불가로 바로 보내는 것처럼 예외를 처리한다.
 *
 * 필터 항목은 기획 4-5 를 따른다. 사이트는 하단 디스크가, 진행 상태는 이 탭 자체가 정하므로
 * 유형·기간·회원 여부와 정렬만 둔다.
 */

/** 접수에서 곧바로 보낼 수 있는 곳. 접수는 지금 자리이므로 빠진다. */
const MOVE_TARGETS = ['IN_PROGRESS', 'DONE', 'REJECTED']
const PAGE_SIZE = 12

export default function IntakePage() {
	const { projectCode } = useParams()
	const { me } = useOutletContext()
	const canEdit = me?.role === 'DEVELOPER'
	const canRequestPriority = me?.role === 'VIEWER'
	const [filters, setFilters] = useState({ sort: 'PRIORITY' })
	const [pagination, setPagination] = useState({ projectCode, page: 0 })
	const page = pagination.projectCode === projectCode ? pagination.page : 0
	const [state, setState] = useState({ projectCode: null, items: [], totalCount: 0, message: '' })
	/** 체크한 피드백 번호들. 일괄 처리의 대상이다. */
	const [picked, setPicked] = useState([])
	/** 담당자 입력을 기다리는 이동. 한 건일 수도 여러 건일 수도 있다. */
	const [pending, setPending] = useState(null)
	const [notice, setNotice] = useState('')
	const [openedId, setOpenedId] = useState(null)
	const [priorityBusy, setPriorityBusy] = useState(false)

	useEffect(() => {
		let cancelled = false

		// 계약대로 필터를 서버에 보낸다. B 의 1단계 작업이 끝나면 서버가 걸러서 내려준다.
		getAdminFeedbacks({ ...filters, project: projectCode, status: 'RECEIVED', page, size: PAGE_SIZE })
			.then((result) => {
				if (!cancelled) {
					setState({ projectCode, items: result.items, totalCount: result.totalCount, message: '' })
					setPicked([])
				}
			})
			.catch((error) => {
				if (!cancelled) setState({ projectCode, items: [], totalCount: 0, message: error.message })
			})

		return () => {
			cancelled = true
		}
	}, [projectCode, filters, page])


	function setPage(nextPage) {
		setPagination({ projectCode, page: nextPage })
	}

	/**
	 * 골라낸 건들을 다른 상태로 보낸다. 먼저 화면에서 빼고 서버에 알린다.
	 * 하나라도 실패하면 전부 되돌린다. 일부만 옮겨진 채로 두면 무엇이 남았는지 알 수 없다.
	 *
	 * @param {number | null} [assigneeId] AssigneeDialog 가 돌려주는 개발자 계정 id.
	 *   id 가 있으면 지정하고, null 이면 해제(unassign: true)로 PATCH 에 함께 싣는다.
	 *   status 변경과 담당자 변경을 요청 하나로 합친다 — PATCH 가 둘 다 받으므로 나눌 이유가 없다.
	 */
	function moveAll(ids, status, assigneeId) {
		if (!canEdit) return
		const before = state.items
		const beforeTotalCount = state.totalCount
		setState((prev) => ({
			...prev,
			items: prev.items.filter((item) => !ids.includes(item.id)),
			totalCount: Math.max(0, prev.totalCount - ids.length),
		}))
		setPicked((prev) => prev.filter((id) => !ids.includes(id)))
		setNotice('')

		Promise.all(ids.map((id) => updateAdminFeedback(id, {
			status,
			...(assigneeId !== undefined
				? assigneeId == null
					? { unassign: true }
					: { assigneeId }
				: {}),
		}))).catch((error) => {
			setState((prev) => ({ ...prev, items: before, totalCount: beforeTotalCount }))
			setNotice(`옮기지 못했습니다. ${error.message}`)
		})
	}

	async function commitChange(id, changes) {
		if (!canEdit) return
		setNotice('')
		try {
			const updated = await updateAdminFeedback(id, changes)
			setState((prev) => {
				const staysInIntake = updated.status === 'RECEIVED'
				return {
					...prev,
					items: staysInIntake
						? prev.items.map((item) => (item.id === id ? { ...item, ...updated } : item))
						: prev.items.filter((item) => item.id !== id),
					totalCount: staysInIntake ? prev.totalCount : Math.max(0, prev.totalCount - 1),
				}
			})
			if (updated.status !== 'RECEIVED') setOpenedId(null)
		} catch (error) {
			setNotice(`바꾸지 못했습니다. ${error.message}`)
		}
	}

	async function requestPriority(id) {
		if (!canRequestPriority || priorityBusy) return
		setPriorityBusy(true)
		setNotice('')
		try {
			const result = await togglePriorityRequest(id)
			setState((prev) => ({
				...prev,
				items: prev.items.map((item) =>
					item.id === id
						? { ...item, priorityRequested: result.priorityRequested, priority: result.priority }
						: item,
				),
			}))
		} catch (error) {
			setNotice(`우선 처리 요청을 저장하지 못했습니다. ${error.message}`)
		} finally {
			setPriorityBusy(false)
		}
	}

	/** 처리 중으로 갈 때만 담당자를 묻는다. 일이 시작되는 시점이기 때문이다. */
	function requestMove(ids, status) {
		if (status === 'IN_PROGRESS') {
			const title =
				ids.length === 1
					? state.items.find((item) => item.id === ids[0])?.title
					: `${ids.length}건을 한꺼번에 옮깁니다`
			setPending({ ids, status, title })
			return
		}
		moveAll(ids, status)
	}

	if (state.projectCode !== projectCode) return <p className="board__notice">불러오는 중…</p>
	if (state.message) return <p className="board__notice">{state.message}</p>

	const visible = state.items
	const opened = state.items.find((item) => item.id === openedId)

	return (
		<div className="intake">
			<FilterBar
				value={filters}
				onChange={(next) => {
					setFilters(next)
					setPage(0)
				}}
			/>

			{notice && <p className="board__alert">{notice}</p>}

			{/* 고른 것이 있을 때만 나타난다. 평소에는 자리를 차지하지 않는다. */}
			{canEdit && picked.length > 0 && (
				<div className="intake__bulk">
					<span>
						<strong>{picked.length}건</strong> 선택됨
					</span>
					<div className="intake__bulk-actions">
						<button type="button" className="intake__ghost" onClick={() => setPicked([])}>
							선택 해제
						</button>
						<button
							type="button"
							className="intake__primary"
							onClick={() => requestMove(picked, 'IN_PROGRESS')}
						>
							처리 시작
						</button>
					</div>
				</div>
			)}

			<p className="intake__count">
				접수 <strong>{state.totalCount}</strong>건
			</p>

			{visible.length === 0 ? (
				<p className="board__empty">조건에 맞는 접수 건이 없습니다.</p>
			) : (
				<div className="intake__list">
					{visible.map((item) => (
						<FeedbackCard
							key={item.id}
							item={item}
							assignee={item.assigneeName}
							selected={picked.includes(item.id)}
							onOpen={() => setOpenedId(item.id)}
							onSelect={canEdit
								? (next) => setPicked((prev) =>
									next ? [...prev, item.id] : prev.filter((id) => id !== item.id),
								)
								: undefined}
							action={canEdit ? (
								<label className="intake__row">
									<span className="intake__muted">상태 변경</span>
									<select
										className="intake__select"
										value=""
										onChange={(event) => requestMove([item.id], event.target.value)}
									>
										<option value="" disabled>
											선택
										</option>
										{MOVE_TARGETS.map((status) => (
											<option key={status} value={status}>
												{labelOf(FEEDBACK_STATUS, status)}
											</option>
										))}
									</select>
								</label>
							) : undefined}
						/>
					))}
				</div>
			)}

			<Pagination page={page} size={PAGE_SIZE} totalCount={state.totalCount} onChange={setPage} />

			{pending && (
				<AssigneeDialog
					feedbackTitle={pending.title}
					onCancel={() => setPending(null)}
					onConfirm={(assigneeId) => {
						moveAll(pending.ids, pending.status, assigneeId)
						setPending(null)
					}}
				/>
			)}

			{opened && (
				<DetailPanel
					item={opened}
					canEdit={canEdit}
					canRequestPriority={canRequestPriority}
					priorityBusy={priorityBusy}
					onChange={(changes) => commitChange(opened.id, changes)}
					onTogglePriority={() => requestPriority(opened.id)}
					onClose={() => setOpenedId(null)}
				/>
			)}
		</div>
	)
}
