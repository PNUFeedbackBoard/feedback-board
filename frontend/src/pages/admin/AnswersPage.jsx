import { useEffect, useState } from 'react'
import { useOutletContext, useParams } from 'react-router-dom'
import { getAdminFeedbacks } from '../../api/endpoints.js'
import AnswerDialog from './AnswerDialog.jsx'
import FeedbackCard from './components/FeedbackCard.jsx'
import StatusBadge from './components/StatusBadge.jsx'
import './intake.css'

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
	const { assignees } = useOutletContext()
	const [state, setState] = useState({ projectCode: null, items: [], message: '' })
	const [writing, setWriting] = useState(null)

	useEffect(() => {
		let cancelled = false

		// 기본 정렬은 오래된순이다. 오래 기다린 것이 위로 온다.
		getAdminFeedbacks({ project: projectCode, sort: 'OLDEST' })
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

	// 답변이 달린 것은 이 화면의 일이 아니다. 답변을 마치는 순간 여기서 빠진다.
	// TODO(C, 2단계): B 의 필터가 붙으면 answered=false 를 서버에 맡기고 이 줄을 지운다.
	const waiting = state.items.filter((item) => !item.answered)
	const answerable = waiting.filter((item) => item.authorType === 'MEMBER').length

	return (
		<div className="intake">
			<p className="intake__count">
				답변 필요 <strong>{answerable}</strong>건
				{waiting.length > answerable && (
					<span className="intake__sub"> · 비회원 {waiting.length - answerable}건은 대상 아님</span>
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
								assignee={assignees[item.id]}
								dimmed={isGuest}
								action={
									<div className="intake__row">
										<StatusBadge status={item.status} />
										{isGuest ? (
											<span className="intake__muted">비회원 · 답변 대상 아님</span>
										) : (
											<button
												type="button"
												className="intake__start"
												onClick={() => setWriting(item)}
											>
												답변하기
											</button>
										)}
									</div>
								}
							/>
						)
					})}
				</div>
			)}

			{writing && (
				<AnswerDialog
					feedback={writing}
					onCancel={() => setWriting(null)}
					onDone={(markedDone) => {
						// 답변을 마쳤으니 이 화면에서 뺀다. 상태까지 바꿨으면 그것도 반영한다.
						setState((prev) => ({
							...prev,
							items: prev.items.map((item) =>
								item.id === writing.id
									? { ...item, answered: true, status: markedDone ? 'DONE' : item.status }
									: item,
							),
						}))
						setWriting(null)
					}}
				/>
			)}
		</div>
	)
}
