import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import PriorityChip from '../../components/common/PriorityChip.jsx'
import StatusBadge from '../../components/common/StatusBadge.jsx'
import { getMe, getMyFeedback } from '../../api/endpoints.js'
import { FEEDBACK_CATEGORY, labelOf } from '../../constants/enums.js'
import UserShell from './UserShell.jsx'
import './my-feedback-detail.css'

/**
 * 경로: /my/:id. 문의 상세. 회원 전용. 기획 5-3.
 *
 * 작성자는 등록한 피드백을 수정·삭제할 수 없다(기획 5-3) — 그래서 이 화면에는
 * 조회용 요소만 있고 입력 칸이 없다. reportedPriority 는 작성자가 고른 참고용 값이라
 * 그대로 보여 주되(PriorityChip), 개발자가 확정한 priority 는 애초에 이 API 가 내려주지
 * 않는다(MyFeedbackDetail 설명 참고 — 사용자에게 내부 처리값을 노출하지 않는다).
 */
export default function MyFeedbackDetailPage() {
	const { id } = useParams()
	const navigate = useNavigate()
	const [state, setState] = useState({ loading: true, detail: null, message: '' })

	useEffect(() => {
		let cancelled = false

		getMe()
			.then(() =>
				getMyFeedback(id).then((detail) => {
					if (!cancelled) setState({ loading: false, detail, message: '' })
				}),
			)
			.catch((problem) => {
				if (cancelled) return
				if (problem.status === 401) {
					navigate('/', { replace: true })
					return
				}
				const message = problem.status === 404 ? '문의를 찾을 수 없습니다.' : problem.message
				setState({ loading: false, detail: null, message })
			})

		return () => {
			cancelled = true
		}
	}, [id, navigate])

	return (
		<UserShell title="문의 상세">
			<div className="my-feedback">
				<Link to="/home" className="my-feedback__back">
					← 내 문의로
				</Link>

				{state.loading && <p className="my-feedback__notice">불러오는 중…</p>}
				{state.message && <p className="my-feedback__notice">{state.message}</p>}

				{state.detail && (
					<>
						<header className="my-feedback__head">
							<span className="my-feedback__site">{state.detail.projectName}</span>
							<h1 className="my-feedback__title">{state.detail.title}</h1>
							<div className="my-feedback__tags">
								<StatusBadge status={state.detail.status} />
								<span className="my-feedback__category">
									{labelOf(FEEDBACK_CATEGORY, state.detail.category)}
								</span>
								<PriorityChip priority={state.detail.reportedPriority} />
							</div>
							<p className="my-feedback__date">{formatDay(state.detail.createdAt)} 등록</p>
						</header>

						<section className="my-feedback__content">{state.detail.content}</section>

						<section className="my-feedback__answer">
							<h2>개발자 답변</h2>
							{state.detail.answer ? (
								<div className="my-feedback__answer-body">
									<p>{state.detail.answer.content}</p>
									<span className="my-feedback__answer-date">
										{formatDay(state.detail.answer.updatedAt)} 작성
									</span>
								</div>
							) : (
								<p className="my-feedback__answer-empty">아직 등록된 답변이 없습니다.</p>
							)}
						</section>
					</>
				)}
			</div>
		</UserShell>
	)
}

/** 'YYYY-MM-DDTHH:mm:ss' 에서 날짜만 떼어 점으로 잇는다. */
function formatDay(value) {
	return value.slice(0, 10).replaceAll('-', '. ')
}
