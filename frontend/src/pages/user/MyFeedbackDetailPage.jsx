import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getMyFeedback } from '../../api/endpoints.js'
import { FEEDBACK_CATEGORY, FEEDBACK_STATUS, PRIORITY, labelOf } from '../../constants/enums.js'
import '../page-shell.css'

export default function MyFeedbackDetailPage() {
  const { id } = useParams()
	const [detail, setDetail] = useState(null)
	const [message, setMessage] = useState('')

	useEffect(() => {
		let cancelled = false
		getMyFeedback(id)
			.then((found) => !cancelled && setDetail(found))
			.catch((error) => !cancelled && setMessage(error.message))
		return () => {
			cancelled = true
		}
	}, [id])

  return (
		<main className="app-page">
			<div className="app-page__inner app-stack">
				<div className="app-actions"><Link className="app-button app-button--secondary" to="/home">← 내 문의</Link></div>
				{message && <p className="app-alert">{message}</p>}
				{!detail && !message && <p className="app-card">불러오는 중…</p>}
				{detail && (
					<>
						<article className="app-card app-stack">
							<div>
								<p className="app-page__eyebrow">FEEDBACK #{detail.id}</p>
								<h1>{detail.title}</h1>
							</div>
							<div className="detail-meta">
								<span>{detail.projectName}</span>
								<span>{labelOf(FEEDBACK_CATEGORY, detail.category)}</span>
								<span>{labelOf(FEEDBACK_STATUS, detail.status)}</span>
								<span>긴급도 {labelOf(PRIORITY, detail.reportedPriority)}</span>
								<time>{formatDate(detail.createdAt)}</time>
							</div>
							<div className="detail-content">{detail.content}</div>
						</article>

						<section className="app-card app-stack">
							<h2>개발자 답변</h2>
							{detail.answer ? (
								<div className="answer-box">
									{detail.answer.content}
									<p className="app-page__muted">{formatDate(detail.answer.updatedAt)}</p>
								</div>
							) : (
								<p className="app-page__muted">아직 등록된 답변이 없습니다.</p>
							)}
						</section>
					</>
				)}
			</div>
		</main>
  )
}

function formatDate(value) {
	return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}
