import { useEffect, useState } from 'react'
import { useOutletContext, useParams } from 'react-router-dom'
import { getAdminFeedbacks } from '../../api/endpoints.js'
import { FEEDBACK_SORT, toOptions } from '../../constants/enums.js'
import AnswerDialog from './AnswerDialog.jsx'
import FeedbackCard from './components/FeedbackCard.jsx'
import Pagination from './components/Pagination.jsx'
import StatusBadge from './components/StatusBadge.jsx'
import './intake.css'

const PAGE_SIZE = 6

/**
 * 답변 — 프로젝트의 전체 피드백을 보면서 답변을 등록·수정하는 화면이다. 기획 6-4
 *
 * 답변 여부와 무관하게 모두 조회하되, 한 번에 전부 내려받지 않고 서버 페이지네이션으로 나눈다.
 * 사용자는 페이지를 스크롤해 훑고 하단에서 다음 페이지로 이동하며 정렬 기준도 바꿀 수 있다.
 *
 * 비회원이 등록한 피드백은 답변 대상이 아니다(기획 6-4). 목록에는 남기되
 * 비활성으로 표시하고 답변 버튼을 주지 않는다. 답변을 기다리는 것이 아니라는 점은 보여야 한다.
 */
export default function AnswersPage() {
	const { projectCode } = useParams()
	const { me } = useOutletContext()
	const canAnswer = me?.role === 'DEVELOPER'
	const [sort, setSort] = useState('LATEST')
	const [pagination, setPagination] = useState({ projectCode, page: 0 })
	const page = pagination.projectCode === projectCode ? pagination.page : 0
	const [state, setState] = useState({ projectCode: null, items: [], totalCount: 0, message: '' })
	const [writing, setWriting] = useState(null)

	useEffect(() => {
		let cancelled = false

		getAdminFeedbacks({ project: projectCode, sort, page, size: PAGE_SIZE })
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
	}, [projectCode, sort, page])


	function setPage(nextPage) {
		setPagination({ projectCode, page: nextPage })
	}

	if (state.projectCode !== projectCode) return <p className="board__notice">불러오는 중…</p>
	if (state.message) return <p className="board__notice">{state.message}</p>

	const items = state.items
	const unanswered = items.filter((item) => item.authorType === 'MEMBER' && !item.answered).length

	return (
		<div className="intake">
			<div className="answers__toolbar">
				<p className="intake__count">
					전체 피드백 <strong>{state.totalCount}</strong>건
					{unanswered > 0 && (
						<span className="intake__sub"> · 현재 페이지 미답변 {unanswered}건</span>
					)}
				</p>
				<label className="answers__sort">
					<span>정렬</span>
					<select
						value={sort}
						onChange={(event) => {
							setSort(event.target.value)
							setPage(0)
						}}
					>
						{toOptions(FEEDBACK_SORT).map((option) => (
							<option key={option.value} value={option.value}>{option.label}</option>
						))}
					</select>
				</label>
			</div>

			{items.length === 0 ? (
				<p className="board__empty">등록된 피드백이 없습니다.</p>
			) : (
				<div className="intake__list">
					{items.map((item) => {
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
		</div>
	)
}
