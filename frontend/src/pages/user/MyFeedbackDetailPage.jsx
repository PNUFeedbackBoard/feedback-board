import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getMe, getMyFeedback } from '../../api/endpoints.js'
import CategoryTag from '../../components/common/CategoryTag.jsx'
import CurrentUserBadge from '../../components/common/CurrentUserBadge.jsx'
import LogoutButton from '../../components/common/LogoutButton.jsx'
import PriorityChip from '../../components/common/PriorityChip.jsx'
import StatusBadge from '../../components/common/StatusBadge.jsx'
import '../page-shell.css'

export default function MyFeedbackDetailPage() {
  const { id } = useParams()
	const [detail, setDetail] = useState(null)
	const [currentUser, setCurrentUser] = useState(null)
	const [message, setMessage] = useState('')

	useEffect(() => {
		let cancelled = false
		Promise.all([getMyFeedback(id), getMe()])
			.then(([found, me]) => {
				if (cancelled) return
				setDetail(found)
				setCurrentUser(me)
			})
			.catch((error) => !cancelled && setMessage(error.message))
		return () => {
			cancelled = true
		}
	}, [id])

  return (
		<main className="app-page">
			<div className="app-page__inner app-stack">
				<header className="app-page__header app-page__header--compact">
					<div className="app-actions"><Link className="app-button app-button--secondary" to="/home">← 내 문의</Link></div>
					<div className="app-header__aside">
						<CurrentUserBadge user={currentUser} />
						<LogoutButton />
					</div>
				</header>
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
								<CategoryTag category={detail.category} />
								<StatusBadge status={detail.status} />
								<PriorityChip priority={detail.reportedPriority} />
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
