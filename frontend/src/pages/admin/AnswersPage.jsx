import { useEffect, useState } from 'react'
import { useOutletContext, useParams } from 'react-router-dom'
import { getAdminFeedbacks } from '../../api/endpoints.js'
import AnswerDialog from './AnswerDialog.jsx'
import FeedbackCard from './components/FeedbackCard.jsx'
import Pagination from './components/Pagination.jsx'
import StatusBadge from './components/StatusBadge.jsx'
import './intake.css'

const PAGE_SIZE = 12

/**
 * 답변 — **답변이 필요한 것만** 올라오는 대기열이다. 기획 6-4
 *
 * 기획의 기본 필터가 `답변 필요`, 기본 정렬이 오래된순인데, 여기서는 그것을 기본값이 아니라
 * **화면의 성격으로 굳혔다.** 답변을 마치면 목록에서 빠진다.
 * 완료된 답변까지 한 화면에 쌓으면 목록만 길어지고 지금 할 일이 묻힌다.
 * 지난 답변은 개발 보드의 처리 완료 열에서 볼 수 있다. **팀 확인이 필요한 기획 변경이다.**
 *
 * 비회원이 등록한 피드백은 답변 대상이 아니다(기획 6-4). 목록에는 남기되
 * 비활성으로 표시하고 답변 버튼을 주지 않는다. 답변을 기다리는 것이 아니라는 점은 보여야 한다.
 */
export default function AnswersPage() {
	const { projectCode } = useParams()
	const { me } = useOutletContext()
	const canAnswer = me?.role === 'DEVELOPER'
	const [pagination, setPagination] = useState({ projectCode, page: 0 })
	const page = pagination.projectCode === projectCode ? pagination.page : 0
	const [state, setState] = useState({ projectCode: null, items: [], totalCount: 0, message: '' })
	const [writing, setWriting] = useState(null)

	useEffect(() => {
		let cancelled = false

		// 기본 정렬은 오래된순이다. 오래 기다린 것이 위로 온다.
		getAdminFeedbacks({ project: projectCode, sort: 'OLDEST', answered: false, page, size: PAGE_SIZE })
			.then((result) => {
				if (!cancelled) {
					setState({
						projectCode,
						items: result.items,
						totalCount: result.totalCount,
						message: '',
					})
				}
			})
			.catch((error) => {
				if (!cancelled) {
					setState({ projectCode, items: [], totalCount: 0, message: error.message })
				}
			})

		return () => {
			cancelled = true
		}
	}, [projectCode, page])


	function setPage(nextPage) {
		setPagination({ projectCode, page: nextPage })
	}

	if (state.projectCode !== projectCode) return <p className="board__notice">불러오는 중…</p>
	if (state.message) return <p className="board__notice">{state.message}</p>

	const waiting = state.items
	const answerable = waiting.filter((item) => item.authorType === 'MEMBER').length

	return (
		<div className="intake">
			<p className="intake__count">
				답변 필요 <strong>{state.totalCount}</strong>건
				{waiting.length > answerable && (
					<span className="intake__sub"> · 현재 페이지의 비회원 {waiting.length - answerable}건은 대상 아님</span>
				)}
			</p>

			{waiting.length === 0 ? (
				<p className="board__empty">답변을 기다리는 문의가 없습니다.</p>
			) : (
				<div className="intake__list">
					{waiting.map((item) => {
						const isGuest = item.authorType === 'GUEST'

						return (
							<FeedbackCard
								key={item.id}
								item={item}
								assignee={item.assigneeName}
								dimmed={isGuest}
								action={
									<div className="intake__row">
										<StatusBadge status={item.status} />
										{isGuest ? (
											<span className="intake__muted">비회원 · 답변 대상 아님</span>
										) : canAnswer ? (
											<button
												type="button"
												className="intake__start"
												onClick={() => setWriting(item)}
											>
												답변하기
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
					onDone={() => {
						// 답변을 마쳤으니 답변 대기열에서 제거한다.
						setState((prev) => ({
							...prev,
							items: prev.items.filter((item) => item.id !== writing.id),
							totalCount: Math.max(0, prev.totalCount - 1),
						}))
						setWriting(null)
					}}
				/>
			)}
		</div>
	)
}
