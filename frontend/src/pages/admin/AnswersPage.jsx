import { useEffect, useState } from 'react'
import { useOutletContext, useParams } from 'react-router-dom'
import { getAdminFeedbacks, togglePriorityRequest, updateAdminFeedback } from '../../api/endpoints.js'
import AnswerDialog from './AnswerDialog.jsx'
import DetailPanel from './DetailPanel.jsx'
import FeedbackCard from './components/FeedbackCard.jsx'
import ListFilters from './components/ListFilters.jsx'
import Pagination from './components/Pagination.jsx'
import './intake.css'

const PAGE_SIZE = 6

/**
 * 목록 — 프로젝트의 모든 피드백을 접수·처리 중·처리 완료·반영 불가 구분 없이 한 화면에 모은다.
 *
 * **팀 확인이 필요한 기획 변경이다.** 기획 6-4 의 "답변" 탭을 "목록"으로 바꾸고, 진행 상태·중요도·
 * 우선 처리 요청·답변 유무·유형·작성자·기간으로 좁혀 보게 했다. 경로(`/answers`)는 routes.jsx 가
 * 공유 파일이라 그대로 둔다.
 *
 * 모든 조건은 서버 쿼리로 보내고 페이지도 서버가 나눈다. 중요도는 거름이 아니라
 * 정렬(중요도순)로 본다.
 *
 * 비회원이 등록한 피드백은 답변 대상이 아니다(기획 6-4). 목록에는 남기되
 * 비활성으로 표시하고 답변 버튼을 주지 않는다. 답변을 기다리는 것이 아니라는 점은 보여야 한다.
 */
export default function AnswersPage() {
	const { projectCode } = useParams()
	const { me } = useOutletContext()
	const canAnswer = me?.role === 'DEVELOPER'
	const canRequestPriority = me?.role === 'VIEWER'
	const [filters, setFilters] = useState({ sort: 'LATEST' })
	const [pagination, setPagination] = useState({ projectCode, page: 0 })
	const page = pagination.projectCode === projectCode ? pagination.page : 0
	const [state, setState] = useState({ projectCode: null, items: [], totalCount: 0, message: '' })
	const [writing, setWriting] = useState(null)
	const [openedId, setOpenedId] = useState(null)
	const [notice, setNotice] = useState('')
	const [priorityBusy, setPriorityBusy] = useState(false)

	useEffect(() => {
		let cancelled = false

		getAdminFeedbacks({
			project: projectCode,
			status: filters.status,
			category: filters.category,
			authorType: filters.authorType,
			answered: filters.answered,
			priorityRequested: filters.priorityRequested,
			from: filters.from,
			to: filters.to,
			sort: filters.sort,
			page,
			size: PAGE_SIZE,
		})
			.then((result) => {
				if (!cancelled) {
					setState({ projectCode, items: result.items, totalCount: result.totalCount, message: '' })
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

	/** 수정 권한이 있을 때 상세 패널에서 바꾼 값을 저장하고 카드에 반영한다. 목록이라 카드는 빠지지 않는다. */
	async function commitChange(id, changes) {
		if (!canAnswer) return
		setNotice('')
		try {
			const updated = await updateAdminFeedback(id, changes)
			setState((prev) => ({
				...prev,
				items: prev.items.map((item) => (item.id === id ? { ...item, ...updated } : item)),
			}))
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

	if (state.projectCode !== projectCode) return <p className="board__notice">불러오는 중…</p>
	if (state.message) return <p className="board__notice">{state.message}</p>

	const unanswered = state.items.filter((item) => item.authorType === 'MEMBER' && !item.answered).length
	const opened = state.items.find((item) => item.id === openedId)

	return (
		<div className="intake">
			<ListFilters
				value={filters}
				onChange={(next) => {
					setFilters(next)
					setPage(0)
				}}
			/>

			{notice && <p className="board__alert">{notice}</p>}
			<p className="intake__count">
				전체 <strong>{state.totalCount}</strong>건
				{unanswered > 0 && <span className="intake__sub"> · 현재 페이지 미답변 {unanswered}건</span>}
			</p>

			{state.items.length === 0 ? (
				<p className="board__empty">조건에 맞는 피드백이 없습니다.</p>
			) : (
				<div className="intake__list">
					{state.items.map((item) => {
						const isGuest = item.authorType === 'GUEST'

						return (
							<FeedbackCard
								key={item.id}
								item={item}
								assignee={item.assigneeName}
								dimmed={isGuest}
								onOpen={() => setOpenedId(item.id)}
								action={
									<div className="intake__row">
										{isGuest ? (
											<span className="intake__muted">비회원 · 답변 대상 아님</span>
										) : canAnswer ? (
											<button
												type="button"
												className="intake__start"
												onClick={() => setWriting(item)}
											>
												{item.answered ? '답변 수정' : '답변하기'}
											</button>
										) : (
											<span className="intake__muted">개발자만 답변 가능</span>
										)}
									</div>
								}
							/>
						)
					})}
				</div>
			)}

			<Pagination page={page} size={PAGE_SIZE} totalCount={state.totalCount} onChange={setPage} />

			{canAnswer && writing && (
				<AnswerDialog
					feedback={writing}
					onCancel={() => setWriting(null)}
					onDone={(markedDone) => {
						// 전체 목록이므로 답변 뒤에도 카드를 유지하고 표시만 최신화한다.
						setState((prev) => ({
							...prev,
							items: prev.items.map((item) => item.id === writing.id
								? {
									...item,
									answered: true,
									status: markedDone ? 'DONE' : item.status,
								}
								: item),
						}))
						setWriting(null)
					}}
				/>
			)}

			{opened && (
				<DetailPanel
					item={opened}
					canEdit={canAnswer}
					canRequestPriority={canRequestPriority}
					priorityBusy={priorityBusy}
					onChange={(changes) => commitChange(opened.id, changes)}
					onTogglePriority={() => requestPriority(opened.id)}
					onAnswer={() => setWriting(opened)}
					onClose={() => setOpenedId(null)}
				/>
			)}
		</div>
	)
}
