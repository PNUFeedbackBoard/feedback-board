import LogoutButton from '../../components/common/LogoutButton.jsx'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMe } from '../../api/endpoints.js'
import '../page-shell.css'

export default function PendingPage() {
	const [me, setMe] = useState(null)
	const [message, setMessage] = useState('')

	useEffect(() => {
		let cancelled = false
		getMe()
			.then((found) => !cancelled && setMe(found))
			.catch((error) => !cancelled && setMessage(error.message))
		return () => {
			cancelled = true
		}
	}, [])

  return (
		<main className="app-page">
			<div className="app-page__inner app-card app-stack">
				<p className="app-page__eyebrow">ACCOUNT STATUS</p>
              <LogoutButton />
				<h1>{me?.status === 'PENDING' ? '승인을 기다리고 있습니다' : '계정 상태 안내'}</h1>
				{message && <p className="app-alert">{message}</p>}
				{!message && !me && <p>계정 상태를 확인하는 중…</p>}
				{me?.status === 'PENDING' && <p className="app-page__muted">{me.name}님의 가입 요청이 접수되었습니다. 개발자가 역할을 지정하고 승인하면 관리 화면을 사용할 수 있습니다.</p>}
				{me?.status === 'DISABLED' && <p className="app-alert">현재 비활성화된 계정입니다. 관리자에게 문의해 주세요.</p>}
				{me?.status === 'ACTIVE' && <p className="app-success">승인이 완료된 계정입니다. 관리 화면으로 이동할 수 있습니다.</p>}
				<div className="app-actions">
					{me?.status === 'ACTIVE' && <Link className="app-button" to="/admin">관리 화면</Link>}
					<Link className="app-button app-button--secondary" to="/">처음으로</Link>
				</div>
			</div>
		</main>
  )
}
