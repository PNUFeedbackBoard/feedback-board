import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyFeedbacks } from '../../api/endpoints.js'
import CategoryTag from '../../components/common/CategoryTag.jsx'
import StatusBadge from '../../components/common/StatusBadge.jsx'
import '../page-shell.css'

export default function HomePage() {
	const [items, setItems] = useState(null)
	const [message, setMessage] = useState('')

	useEffect(() => {
		let cancelled = false
		getMyFeedbacks()
			.then((found) => !cancelled && setItems(found))
			.catch((error) => !cancelled && setMessage(error.message))
		return () => {
			cancelled = true
		}
	}, [])

  return (
		<main className="app-page">
			<div className="app-page__inner app-stack">
				<header className="app-page__header">
					<div>
						<p className="app-page__eyebrow">MY FEEDBACK</p>
						<h1>내 문의</h1>
						<p className="app-page__muted">등록한 피드백의 처리 상태와 답변을 확인합니다.</p>
					</div>
					<Link className="app-button" to="/write">새 문의 작성</Link>
				</header>

				{message && <p className="app-alert">{message}</p>}
				{items === null && !message && <p className="app-card">불러오는 중…</p>}
				{items?.length === 0 && <p className="app-card">아직 등록한 문의가 없습니다.</p>}
				{items?.length > 0 && (
					<ul className="feedback-list">
						{items.map((item) => (
							<li key={item.id}>
								<Link className="feedback-list__item" to={`/my/${item.id}`}>
									<strong>{item.title}</strong>
									<span className="feedback-list__meta">
										<span>{item.projectName}</span>
										<CategoryTag category={item.category} />
										<StatusBadge status={item.status} />
										<span>{item.answered ? '답변 완료' : '답변 대기'}</span>
										<time>{formatDay(item.createdAt)}</time>
									</span>
								</Link>
							</li>
						))}
					</ul>
				)}
			</div>
		</main>
  )
}

function formatDay(value) {
	return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(value))
}
