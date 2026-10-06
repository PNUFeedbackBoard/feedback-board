import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { devLogin, getMe } from '../../api/endpoints.js'
import '../page-shell.css'
import iniLogo from '../../assets/ini-logo.png'
import './site-logo.css'

export default function LoginPage() {
	const navigate = useNavigate()
	const [busy, setBusy] = useState(false)
	const [message, setMessage] = useState('')
	const [guestNotice, setGuestNotice] = useState(false)

	const goToStart = useCallback((me) => {
		if (me.status === 'PENDING') navigate('/admin/pending')
		else if (me.role === 'DEVELOPER' || me.role === 'VIEWER') navigate('/admin')
		else navigate('/home')
	}, [navigate])

	useEffect(() => {
		getMe()
			.then(goToStart)
			.catch(() => {})
	}, [goToStart])

	async function login() {
		if (!import.meta.env.DEV) {
			setMessage('운영 로그인은 Google OAuth 설정이 완료된 뒤 제공됩니다.')
			return
		}
		setBusy(true)
		setMessage('')
		try {
			goToStart(await devLogin('user'))
		} catch (error) {
			setMessage(error.message)
		} finally {
			setBusy(false)
		}
	}

  return (
		<main className="app-page">
			<div className="app-page__inner app-stack">
				<header className="app-card">
					<img className="login-brand__mark" src={iniLogo} alt="INI 로고" />
					<p className="app-page__eyebrow">ISSUE &amp; IDEA</p>
					<h1>통합 피드백 보드</h1>
					<p className="app-page__muted">
						부산대학교 AI융합교육원 서비스에서 발견한 문제와 아이디어를 한곳에 남겨 주세요.
					</p>
					<div className="app-actions">
						<button className="app-button" type="button" onClick={login} disabled={busy}>
							{busy ? '로그인 중…' : import.meta.env.DEV ? '회원 데모 로그인' : 'Google로 로그인'}
						</button>
						<button className="app-button app-button--secondary" type="button" onClick={() => setGuestNotice(true)}>
							비회원으로 계속
						</button>
					</div>
				</header>

				{message && <p className="app-alert">{message}</p>}

				{guestNotice && (
					<section className="app-card" role="dialog" aria-modal="true" aria-labelledby="guest-title">
						<h2 id="guest-title">비회원으로 작성할까요?</h2>
						<p className="app-page__muted">
							비회원 문의는 제출 후 진행 상태와 개발자 답변을 다시 확인할 수 없습니다.
						</p>
						<div className="app-actions">
							<button className="app-button" type="button" onClick={() => navigate('/write')}>
								계속 작성
							</button>
							<button className="app-button app-button--secondary" type="button" onClick={() => setGuestNotice(false)}>
								취소
							</button>
						</div>
					</section>
				)}
			</div>
		</main>
  )
}
