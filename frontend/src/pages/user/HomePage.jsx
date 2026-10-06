import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import StatusBadge from '../../components/common/StatusBadge.jsx'
import { getMe, getMyFeedbacks } from '../../api/endpoints.js'
import { FEEDBACK_CATEGORY, labelOf } from '../../constants/enums.js'
import UserShell from './UserShell.jsx'
import './home.css'

/**
 * 경로: /home. 홈 — 내 문의 목록. 회원 전용. 기획 5-3.
 *
 * 회원 전용 화면이지만 경로 자체를 막지는 않는다(기획 2장 — 권한 검사는 화면이 아니라
 * API 가 한다). 비회원이 들어오면 getMe() 가 401 을 주므로 로그인 화면으로 돌려보낸다.
 */
export default function HomePage() {
	const navigate = useNavigate()
	const [state, setState] = useState({ loading: true, items: null, message: '' })

	useEffect(() => {
		let cancelled = false

		getMe()
			.then(() =>
				getMyFeedbacks().then((items) => {
					if (!cancelled) setState({ loading: false, items, message: '' })
				}),
			)
			.catch((problem) => {
				if (cancelled) return
				if (problem.status === 401) {
					navigate('/', { replace: true })
					return
				}
				setState({ loading: false, items: null, message: problem.message })
			})

		return () => {
			cancelled = true
		}
	}, [navigate])

	return (
		<UserShell title="내 문의">
			<div className="home">
				<div className="home__head">
					<h1 className="home__title">내 문의</h1>
					<Link to="/write" className="home__write">
						새 질문 적기
					</Link>
				</div>

				{state.loading && <p className="home__notice">불러오는 중…</p>}
				{state.message && <p className="home__notice">{state.message}</p>}

				{state.items && state.items.length === 0 && (
					<p className="home__empty">아직 등록한 문의가 없습니다. 새 질문을 적어 보세요.</p>
				)}

				{state.items && state.items.length > 0 && (
					<ul className="home__list">
						{state.items.map((item) => (
							<li key={item.id}>
								<Link to={`/my/${item.id}`} className="home__row">
									<div className="home__row-main">
										<span className="home__site">{item.projectName}</span>
										<p className="home__row-title">{item.title}</p>
									</div>
									<div className="home__row-meta">
										<span className="home__category">{labelOf(FEEDBACK_CATEGORY, item.category)}</span>
										<StatusBadge status={item.status} />
										<span className="home__date">{formatDay(item.createdAt)}</span>
										<span className="home__answered">{item.answered ? '답변 완료' : '답변 없음'}</span>
									</div>
								</Link>
							</li>
						))}
					</ul>
				)}
			</div>
		</UserShell>
	)
}

/** 'YYYY-MM-DDTHH:mm:ss' 에서 날짜만 떼어 점으로 잇는다. */
function formatDay(createdAt) {
	return createdAt.slice(0, 10).replaceAll('-', '. ')
}
